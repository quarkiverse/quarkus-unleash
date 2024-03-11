package io.quarkiverse.unleash.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.unleash.UnleashJsonMapper;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;

public class UnleashResourceProducer {

    @Produces
    @Singleton
    @Unremovable
    @DefaultBean
    public UnleashJsonMapper jsonb(ObjectMapper objectMapper) {
        return new DefaultUnleashJsonMapper(objectMapper);
    }

}
