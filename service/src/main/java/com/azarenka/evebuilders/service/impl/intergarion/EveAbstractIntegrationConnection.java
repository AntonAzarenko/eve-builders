package com.azarenka.evebuilders.service.impl.intergarion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import jakarta.annotation.PostConstruct;
import reactor.netty.http.client.HttpClient;

public abstract class EveAbstractIntegrationConnection {

    WebClient webClient;

    @Value("${eve.webclient.baseUrl}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                .responseTimeout(Duration.ofMinutes(2))
            ))
            .build();
    }
}
