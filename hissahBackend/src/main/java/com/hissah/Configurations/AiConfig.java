package com.hissah.Configurations;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    @Bean
    public RestClient openAiRestClient(
            RestClient.Builder builder,
            AiProperties properties
    ) {
        return builder
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
