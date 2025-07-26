package com.azarenka.evebuilders.service.impl.inventory;

import com.azarenka.evebuilders.domain.db.Asset;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.ItemDto;
import com.azarenka.evebuilders.domain.sqllite.EveIcon;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.repository.litesql.EveIconRepository;
import com.azarenka.evebuilders.repository.litesql.InvTypesRepository;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.IUserTokenService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.azarenka.evebuilders.service.impl.auth.TokenRefreshService;
import com.azarenka.evebuilders.service.impl.intergarion.AssetIntegrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
public class AssetService {

    @Autowired
    private IUserService userService;
    @Autowired
    private IUserTokenService tokenService;
    @Autowired
    private TokenRefreshService tokenRefreshService;
    @Autowired
    private InvTypesRepository itemRepository;
    @Autowired
    private EveIconRepository eveIconRepository;
    @Autowired
    private AssetIntegrationService assetIntegrationService;

    public List<ItemDto> getMinerals(List<String> expectedMaterials) {
        List<Integer> mineralTypeIds = itemRepository.findTypeIdsByNames(expectedMaterials);
        var mainCharacterId = userService.getCharacterId();
        var mainUserToken = userService.getUserToken();
        var alters = userService.getAlters();
        var allAssets =
            new ArrayList<>(findAssets(SecurityUtils.getUserName(), mainCharacterId, mainUserToken, mineralTypeIds));
        alters.forEach(alter -> {
            var updatedAccessToken = tokenRefreshService
                .refreshTokenIfNeeded(alter.getUid())
                .defaultIfEmpty(tokenService.getUserToken(alter.getUid()))
                .block();
            allAssets.addAll(
                findAssets(alter.getUsername(), alter.getCharacterId(), updatedAccessToken, mineralTypeIds));
        });
        return allAssets;
    }

    public List<Asset> groupAssetsByTypeIdAndSumQuantity(List<Asset> assets) {
        return assets.stream()
            .collect(Collectors.groupingBy(
                Asset::getTypeId,
                Collectors.summingInt(asset -> asset.getQuantity() != null ? asset.getQuantity() : 0)
            ))
            .entrySet().stream()
            .map(entry -> {
                Asset asset = new Asset();
                asset.setTypeId(entry.getKey());
                asset.setQuantity(entry.getValue());
                return asset;
            })
            .toList();
    }

    private List<ItemDto> findAssets(String userName, String characterId, String userToken,
                                     List<Integer> mineralTypeIds) {
        var assets = assetIntegrationService.findAssets(characterId, userToken)
            .stream()
            .filter(asset -> mineralTypeIds.contains(asset.getTypeId()))
            .toList();
        assets = groupAssetsByTypeIdAndSumQuantity(assets);
        List<InvType> invTypes = itemRepository.findByTypeIDIn(assets.stream().map(Asset::getTypeId).toList());
        List<EveIcon> eveIcons = eveIconRepository.findByIconIdIn(invTypes.stream().map(InvType::getIconID).toList());
        return assets.stream()
            .map(asset -> {
                InvType invType = invTypes.stream()
                    .filter(type -> type.getTypeID().equals(asset.getTypeId()))
                    .findFirst()
                    .orElse(null);
                EveIcon eveIcon = (invType != null) ? eveIcons.stream()
                    .filter(icon -> icon.getIconId().equals(invType.getIconID()))
                    .findFirst()
                    .orElse(null) : null;
                var dto = new ItemDto();
                dto.setAsset(asset);
                dto.setInvType(invType);
                dto.setEveIcon(eveIcon);
                dto.setUserName(userName);
                return dto;
            })
            .toList();
    }
}
