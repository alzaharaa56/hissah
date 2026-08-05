package com.hissah.Services;

import java.util.Map;

public interface AiClientService {

    <T> T generateStructuredResponse(
            String instructions,
            String input,
            String schemaName,
            Map<String, Object> schema,
            Class<T> responseType
    );
}
