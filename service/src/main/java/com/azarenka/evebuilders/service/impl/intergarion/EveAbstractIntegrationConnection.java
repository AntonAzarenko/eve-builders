package com.azarenka.evebuilders.service.impl.intergarion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

public abstract class EveAbstractIntegrationConnection {

    WebClient webClient;

    @Value("${eve.webclient.baseUrl}")
    private String baseUrl;

    public EveAbstractIntegrationConnection() {
        this.webClient = WebClient.builder()
                .baseUrl("https://esi.evetech.net")
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                        .responseTimeout(Duration.ofMinutes(2))
                ))
                .build();
    }
}
