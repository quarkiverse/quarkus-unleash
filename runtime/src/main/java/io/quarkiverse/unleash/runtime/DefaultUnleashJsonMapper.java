package io.quarkiverse.unleash.runtime;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.unleash.UnleashJsonMapper;
import io.quarkus.arc.DefaultBean;

@Singleton
@DefaultBean
public class DefaultUnleashJsonMapper implements UnleashJsonMapper {

    @Inject
    ObjectMapper objectMapper;

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
