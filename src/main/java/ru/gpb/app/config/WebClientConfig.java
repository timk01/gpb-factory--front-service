package ru.gpb.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient mainWebClient() {
        return WebClient.builder().baseUrl("{khasmamedov-middle-service.url}").build();
    }
}