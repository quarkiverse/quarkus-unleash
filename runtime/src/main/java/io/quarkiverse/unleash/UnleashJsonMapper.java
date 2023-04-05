package io.quarkiverse.unleash;

public interface UnleashJsonMapper {

    <T> T fromJson(final String json, final Class<T> clazz);
}
