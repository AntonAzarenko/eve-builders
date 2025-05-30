package com.azarenka.evebuilders.service.impl.inventory;

import com.azarenka.evebuilders.domain.dto.ItemDto;
import com.azarenka.evebuilders.domain.mysql.Asset;
import com.azarenka.evebuilders.domain.sqllite.EveIcon;
import com.azarenka.evebuilders.domain.sqllite.InvType;
import com.azarenka.evebuilders.repository.litesql.EveIconRepository;
import com.azarenka.evebuilders.repository.litesql.InvTypesRepository;
import com.azarenka.evebuilders.service.api.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssetService {

    private final WebClient webClient;

    @Value("${eve.character.assets.url}")
    private String apiUrl;

    @Autowired
    private IUserService userService;

    private final InvTypesRepository itemRepository;
    private final EveIconRepository eveIconRepository;

    public AssetService(InvTypesRepository itemRepository, EveIconRepository eveIconRepository) {
        this.webClient = WebClient.builder()
                .baseUrl("https://esi.evetech.net")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofMinutes(2))
                ))
                .build();
        this.itemRepository = itemRepository;
        this.eveIconRepository = eveIconRepository;
    }

    public List<ItemDto> getMinerals() {
        String characterId = userService.getCharacterId();
        String userToken = userService.getUserToken();

        int totalPages = getTotalPages(characterId, userToken);
        List<Asset> allAssets = new ArrayList<>();
        for (int page = 1; page <= totalPages; page++) {
            int finalPage = page;
            List<Asset> assetsPage = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(apiUrl)
                            .queryParam("datasource", "tranquility")
                            .queryParam("page", finalPage) // Укажите страницу, если необходимо
                            .build(characterId))
                    .header("Authorization", "Bearer " + userToken)
                    .retrieve()
                    .bodyToFlux(Asset.class)
                    .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(10)))
                    .collectList()
                    .block();
            if (assetsPage != null) {
                allAssets.addAll(assetsPage);
            }
        }
        // 2. Загружаем typeID минералов из базы данных
        List<Integer> mineralTypeIds = itemRepository.findMineralTypeIds();

        List<Asset> temp = allAssets.stream()
                .filter(asset -> mineralTypeIds.contains(asset.getTypeId()))
                .toList();
        temp = groupAssetsByTypeIdAndSumQuantity(temp);
        List<InvType> invTypes = itemRepository.findByTypeIDIn(temp.stream().map(Asset::getTypeId).toList());
        List<EveIcon> eveIcons = eveIconRepository.findByIconIdIn(invTypes.stream().map(InvType::getIconID).toList());
        return temp.stream()
                .map(asset -> {
                    // Ищем InvType для текущего Asset
                    InvType invType = invTypes.stream()
                            .filter(type -> type.getTypeID().equals(asset.getTypeId()))
                            .findFirst()
                            .orElse(null);

                    // Ищем EveIcon для текущего InvType
                    EveIcon eveIcon = (invType != null) ? eveIcons.stream()
                            .filter(icon -> icon.getIconId().equals(invType.getIconID()))
                            .findFirst()
                            .orElse(null) : null;

                    // Создаем ItemDto
                    ItemDto dto = new ItemDto();
                    dto.setAsset(asset);
                    dto.setInvType(invType);
                    dto.setEveIcon(eveIcon);

                    return dto;
                })
                .toList();
    }

    private int getTotalPages(String characterId, String accessToken) {
        WebClient.ResponseSpec responseSpec = webClient.get()
                .uri("https://esi.evetech.net/latest/characters/{characterId}/assets/?datasource=tranquility&page=1",
                        characterId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve();

        return responseSpec.toBodilessEntity()
                .block()
                .getHeaders()
                .getFirst("X-Pages") != null
                ? Integer.parseInt(responseSpec.toBodilessEntity().block().getHeaders().getFirst("X-Pages"))
                : 1;
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
}
