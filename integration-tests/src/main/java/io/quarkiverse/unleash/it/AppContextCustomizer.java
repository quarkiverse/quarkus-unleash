package io.quarkiverse.unleash.it;

import jakarta.enterprise.context.*;
import jakarta.ws.rs.core.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.getunleash.UnleashContext;
import io.quarkiverse.unleash.runtime.UnleashContextCustomizer;
import io.quarkus.logging.Log;

@ApplicationScoped
public class AppContextCustomizer implements UnleashContextCustomizer {

    @ConfigProperty(name = "environment", defaultValue = "default")
    String environment;

    @Override
    public void apply(UnleashContext.Builder builder) {
        Log.info("Applying AppContextCustomizer");
        builder.environment(environment);
    }

    @Override
    public boolean isApplicationContext() {
        return true;
    }
}
