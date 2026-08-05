package com.hissah.Configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {

    private boolean enabled = false;
    private boolean mockEnabled = true;
    private String baseUrl = "https://api.openai.com";
    private String apiKey = "";
    private String model = "gpt-5.6";
    private int maxOutputTokens = 1200;
    private int matchingCandidateLimit = 8;
}
