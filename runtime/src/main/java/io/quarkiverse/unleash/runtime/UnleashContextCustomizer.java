package io.quarkiverse.unleash.runtime;

import io.getunleash.UnleashContext;

public interface UnleashContextCustomizer {

    void apply(UnleashContext.Builder builder);

    default boolean isApplicationContext() {
        return false;
    }
}
