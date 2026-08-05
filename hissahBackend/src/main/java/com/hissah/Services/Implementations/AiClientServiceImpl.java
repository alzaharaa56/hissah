package com.hissah.Services.Implementations;

import com.hissah.Configurations.AiProperties;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Services.AiClientService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiClientServiceImpl implements AiClientService {

    private static final Logger log =
            LoggerFactory.getLogger(AiClientServiceImpl.class);

    private final RestClient openAiRestClient;
    private final AiProperties properties;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    public <T> T generateStructuredResponse(
            String instructions,
            String input,
            String schemaName,
            Map<String, Object> schema,
            Class<T> responseType
    ) {
        validateConfiguration();

        Map<String, Object> format = new LinkedHashMap<>();
        format.put("type", "json_schema");
        format.put("name", schemaName);
        format.put("strict", true);
        format.put("schema", schema);

        Map<String, Object> text = new LinkedHashMap<>();
        text.put("format", format);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", properties.getModel());
        requestBody.put("store", false);
        requestBody.put("instructions", instructions);
        requestBody.put("input", input);
        requestBody.put("max_output_tokens", properties.getMaxOutputTokens());
        requestBody.put("text", text);

        try {
            ResponseEntity<JsonNode> responseEntity = openAiRestClient
                    .post()
                    .uri("/v1/responses")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + properties.getApiKey().trim()
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toEntity(JsonNode.class);

            String requestId = responseEntity
                    .getHeaders()
                    .getFirst("x-request-id");

            if (requestId != null) {
                log.info("OpenAI request completed. requestId={}", requestId);
            }

            JsonNode root = responseEntity.getBody();
            String outputText = extractOutputText(root);

            return jsonMapper.readValue(outputText, responseType);

        } catch (BusinessRuleException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("OpenAI request failed.", exception);
            throw new BusinessRuleException(
                    "The AI request failed. Check OPENAI_API_KEY, OPENAI_MODEL, internet access, and account quota."
            );
        }
    }

    private void validateConfiguration() {
        if (!properties.isEnabled()) {
            throw new BusinessRuleException(
                    "AI features are disabled. Set AI_ENABLED=true."
            );
        }

        if (properties.isMockEnabled()) {
            throw new BusinessRuleException(
                    "The real AI client was called while AI mock mode is enabled."
            );
        }

        if (properties.getApiKey() == null
                || properties.getApiKey().isBlank()) {
            throw new BusinessRuleException(
                    "OPENAI_API_KEY is missing. Add it to the backend environment variables."
            );
        }

        if (properties.getModel() == null
                || properties.getModel().isBlank()) {
            throw new BusinessRuleException(
                    "OPENAI_MODEL is missing."
            );
        }
    }

    private String extractOutputText(JsonNode root) {
        if (root == null) {
            throw new BusinessRuleException(
                    "The AI provider returned an empty response."
            );
        }

        if ("incomplete".equals(root.path("status").asText())) {
            String reason = root
                    .path("incomplete_details")
                    .path("reason")
                    .asText("unknown reason");

            throw new BusinessRuleException(
                    "The AI response was incomplete: " + reason
            );
        }

        JsonNode outputItems = root.path("output");

        if (outputItems.isArray()) {
            for (JsonNode outputItem : outputItems) {
                JsonNode contentItems = outputItem.path("content");

                if (!contentItems.isArray()) {
                    continue;
                }

                for (JsonNode contentItem : contentItems) {
                    String type = contentItem.path("type").asText();

                    if ("output_text".equals(type)) {
                        String text = contentItem.path("text").asText();

                        if (!text.isBlank()) {
                            return text;
                        }
                    }

                    if ("refusal".equals(type)) {
                        throw new BusinessRuleException(
                                "The AI provider refused this request: "
                                        + contentItem.path("refusal").asText()
                        );
                    }
                }
            }
        }

        throw new BusinessRuleException(
                "The AI provider returned no structured output text."
        );
    }
}
