package io.quarkiverse.unleash.runtime;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.unleash.UnleashJsonMapper;

public class DefaultUnleashJsonMapper implements UnleashJsonMapper {

    private final ObjectMapper objectMapper;

    public DefaultUnleashJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "The given string value: " + json + " cannot be transformed to Json object: " + clazz,
                    e);
        }
    }
}
