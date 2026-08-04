package com.hissah.Services.Implementations.Support;

import com.hissah.Exceptions.BusinessRuleException;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ServiceDtoMapper {

    private final JsonMapper objectMapper;

    public String text(Object source, String... propertyNames) {
        Object value = firstValue(source, propertyNames);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    public Long longValue(Object source, String... propertyNames) {
        Object value = firstValue(source, propertyNames);
        return convert(value, Long.class);
    }

    public LocalDate dateValue(Object source, String... propertyNames) {
        Object value = firstValue(source, propertyNames);
        return convert(value, LocalDate.class);
    }

    public MultipartFile fileValue(Object source, String... propertyNames) {
        Object value = firstValue(source, propertyNames);
        return value instanceof MultipartFile file ? file : null;
    }

    public List<Long> longList(Object source, String... propertyNames) {
        Object value = firstValue(source, propertyNames);
        if (value == null) {
            return List.of();
        }

        Collection<?> values;
        if (value instanceof Collection<?> collection) {
            values = collection;
        } else {
            values = List.of(value);
        }

        List<Long> result = new ArrayList<>();
        for (Object item : values) {
            Long converted = convert(item, Long.class);
            if (converted != null) {
                result.add(converted);
            }
        }
        return result;
    }

    public <E extends Enum<E>> E enumValue(
            Object source,
            Class<E> enumType,
            String... propertyNames
    ) {
        Object value = firstValue(source, propertyNames);
        if (value == null) {
            return null;
        }
        if (enumType.isInstance(value)) {
            return enumType.cast(value);
        }

        try {
            return Enum.valueOf(
                    enumType,
                    String.valueOf(value).trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException(
                    "Invalid " + enumType.getSimpleName() + " value: " + value
            );
        }
    }

    public <T> T toDto(Map<String, Object> values, Class<T> dtoType) {
        return objectMapper.convertValue(values, dtoType);
    }

    public <T> T convert(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        }
        return objectMapper.convertValue(value, targetType);
    }

    private Object firstValue(Object source, String... propertyNames) {
        if (source == null || propertyNames == null) {
            return null;
        }

        BeanWrapper wrapper = new BeanWrapperImpl(source);

        for (String propertyName : propertyNames) {
            if (propertyName == null || propertyName.isBlank()) {
                continue;
            }
            try {
                if (wrapper.isReadableProperty(propertyName)) {
                    Object value = wrapper.getPropertyValue(propertyName);
                    if (value != null) {
                        return value;
                    }
                }
            } catch (RuntimeException ignored) {
                // Try the next supported alias.
            }
        }
        return null;
    }
}

