package com.azarenka.evebuilders.service.impl.inventory;

import com.azarenka.evebuilders.domain.db.Asset;
import com.azarenka.evebuilders.domain.db.AssetEntity;
import com.azarenka.evebuilders.domain.dto.ItemDto;
import com.azarenka.evebuilders.domain.sqllite.EveIcon;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.repository.database.AssetRepository;
import com.azarenka.evebuilders.repository.litesql.EveIconRepository;
import com.azarenka.evebuilders.repository.litesql.InvTypesRepository;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.IUserTokenService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.azarenka.evebuilders.service.impl.auth.TokenRefreshService;
import com.azarenka.evebuilders.service.impl.intergarion.AssetIntegrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssetService {

    Logger LOGGER = LoggerFactory.getLogger(AssetService.class);

    private final AssetMapper assetMapper = new AssetMapper();

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
    @Autowired
    private CharacterAssetSyncService characterAssetSyncService;
    @Autowired
    private AssetRepository assetRepository;

    @Transactional
    public List<ItemDto> getMaterials(List<String> expectedMaterials) {
        var materialTypeIds = itemRepository.findTypeIdsByNames(expectedMaterials);
        var mainCharacterId = userService.getCharacterId();
        var mainUserToken = userService.getUserToken();
        var alters = userService.getAlters();
        var allAssets = new ArrayList<>(
            syncAndFetchAssets(SecurityUtils.getUserName(), mainCharacterId, mainUserToken, materialTypeIds));
        alters.forEach(alter -> {
            var updatedAccessToken = tokenRefreshService
                .refreshTokenIfNeeded(alter.getUid())
                .defaultIfEmpty(tokenService.getUserToken(alter.getUid()))
                .block();
            allAssets.addAll(syncAndFetchAssets(
                alter.getUsername(), alter.getCharacterId(), updatedAccessToken, materialTypeIds));
        });
        LOGGER.info("Retrieving materials. Stop. UserCount={}, AssetsCount={}", alters.size() + 1, allAssets.size());
        return allAssets;
    }

    private List<ItemDto> syncAndFetchAssets(String userName, String characterId, String userToken,
                                             List<Integer> typeFilter) {
        LOGGER.info("Retrieving materials. Start. UserName={}", userName);
        var savedEtag = characterAssetSyncService.getEtagForUser(userName);
        var response = assetIntegrationService.findAssetsWithEtag(characterId, userToken, savedEtag);
        List<Asset> assets;
        if (response.isNotModified()) {
            characterAssetSyncService.updateExpiresOnly(userName, response.getExpiresAt());
            assets = assetRepository.findAllByUserNameAndTypeIdIn(userName, typeFilter)
                .stream().map(assetMapper::toApiAsset).toList();
            LOGGER.info("Retrieved materials from Database. AssetsCount={}", assets.size());
        } else {
            characterAssetSyncService.updateSync(userName, response.getEtag(), response.getExpiresAt());
            saveAssets(userName, response.getAssets()); // обновим БД
            assets = response.getAssets().stream()
                .filter(asset -> typeFilter.contains(asset.getTypeId()))
                .toList();
        }
        assets = groupAssetsByTypeIdAndSumQuantity(assets);
        List<InvType> invTypes = itemRepository.findByTypeIDIn(assets.stream().map(Asset::getTypeId).toList());
        List<EveIcon> eveIcons = eveIconRepository.findByIconIdIn(invTypes.stream().map(InvType::getIconID).toList());

        return assets.stream()
            .map(asset -> {
                InvType invType = invTypes.stream()
                    .filter(type -> type.getTypeID().equals(asset.getTypeId()))
                    .findFirst()
                    .orElse(null);
                EveIcon eveIcon = (invType != null)
                    ? eveIcons.stream()
                    .filter(icon -> icon.getIconId().equals(invType.getIconID()))
                    .findFirst()
                    .orElse(null)
                    : null;
                var dto = new ItemDto();
                dto.setAsset(asset);
                dto.setInvType(invType);
                dto.setEveIcon(eveIcon);
                dto.setUserName(userName);
                return dto;
            })
            .toList();
    }

    private List<Asset> groupAssetsByTypeIdAndSumQuantity(List<Asset> assets) {
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

    private void saveAssets(String userName, List<Asset> assets) {
        LOGGER.info("Saving assets. UserName={}", userName);
        assetRepository.deleteAllByUserName(userName);
        List<AssetEntity> entities = assets.stream()
            .map(asset -> new AssetEntity(
                UUID.randomUUID().toString(),
                userName,
                asset.getTypeId(),
                asset.getLocationId(),
                asset.getQuantity(),
                LocalDateTime.now()
            ))
            .toList();
        assetRepository.saveAll(entities);
    }

    public void syncUserAssets(String characterId, String userName, String accessToken) {
        var etag = characterAssetSyncService.getEtagForUser(userName);
        var response = assetIntegrationService.findAssetsWithEtag(characterId, accessToken, etag);
        if (response.isNotModified()) {
            characterAssetSyncService.updateExpiresOnly(userName, response.getExpiresAt());
            return;
        }
        saveAssets(userName, response.getAssets());
        characterAssetSyncService.updateSync(userName, response.getEtag(), response.getExpiresAt());
    }

    private static class AssetMapper {

        public Asset toApiAsset(AssetEntity entity) {
            Asset asset = new Asset();
            asset.setTypeId(entity.getType());
            asset.setLocationId(entity.getLocationId());
            asset.setQuantity(entity.getQuantity());
            return asset;
        }

        public AssetEntity toEntity(Asset asset, String userName) {
            AssetEntity entity = new AssetEntity();
            entity.setId(userName + "-" + asset.getTypeId() + "-" + asset.getLocationId());
            entity.setUserName(userName);
            entity.setTypeId(asset.getTypeId());
            entity.setLocationId(asset.getLocationId());
            entity.setQuantity(asset.getQuantity());
            return entity;
        }
    }
}
