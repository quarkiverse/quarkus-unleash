package io.quarkiverse.unleash.runtime;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.unleash.UnleashJsonMapper;
import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;

@Singleton
public class UnleashResourceProducer {

    @Produces
    @Singleton
    @Unremovable
    @DefaultBean
    public UnleashJsonMapper jsonb(ObjectMapper objectMapper) {
        return new DefaultUnleashJsonMapper(objectMapper);
    }

}
