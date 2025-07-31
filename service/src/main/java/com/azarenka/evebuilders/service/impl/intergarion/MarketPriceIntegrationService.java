package com.azarenka.evebuilders.service.impl.intergarion;

import com.azarenka.evebuilders.domain.dto.MarketOrderDto;
import com.azarenka.evebuilders.domain.dto.MarketPriceInfo;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MarketPriceIntegrationService  extends EveAbstractIntegrationConnection  {

    private static final String MARKET_URL_TEMPLATE = "/markets/10000002/orders/?type_id={type_id}&order_type=all&datasource=tranquility";

    public Mono<MarketPriceInfo> getMarketPrices(Integer typeId) {
        return webClient.get()
            .uri(MARKET_URL_TEMPLATE, typeId)
            .retrieve()
            .bodyToFlux(MarketOrderDto.class)
            .collectList()
            .map(orders -> {
                var buy = orders.stream()
                    .filter(MarketOrderDto::isBuyOrder)
                    .map(MarketOrderDto::getPrice)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

                var sell = orders.stream()
                    .filter(o -> !o.isBuyOrder())
                    .map(MarketOrderDto::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

                return new MarketPriceInfo(typeId, buy, sell);
            });
    }

    public List<MarketPriceInfo> getMarketPricesFor(List<Integer> typeIds) {
        return Flux.fromIterable(typeIds)
            .flatMap(this::getMarketPrices)
            .collectList()
            .block();
    }
}
