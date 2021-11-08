package io.quarkiverse.unleash.runtime;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.getunleash.MoreOperations;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.Variant;
import io.quarkus.runtime.ApplicationConfig;
import io.quarkus.runtime.ShutdownEvent;

@ApplicationScoped
public class UnleashService implements Unleash {

    Unleash client;

    void initialize(UnleashRuntimeTimeConfig config, ApplicationConfig appConfig) {
        client = UnleashCreator
                .createUnleash(config,
                        config.appName.orElse(appConfig.name.orElse("default-app-name")));
    }

    void onStop(@Observes ShutdownEvent event) {
        client.shutdown();
    }

    @Override
    public boolean isEnabled(String toggleName) {
        return client.isEnabled(toggleName);
    }

    @Override
    public boolean isEnabled(String toggleName, boolean defaultSetting) {
        return client.isEnabled(toggleName, defaultSetting);
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context) {
        return client.getVariant(toggleName, context);
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context, Variant defaultValue) {
        return client.getVariant(toggleName, context, defaultValue);
    }

    @Override
    public Variant getVariant(String toggleName) {
        return client.getVariant(toggleName);
    }

    @Override
    public Variant getVariant(String toggleName, Variant defaultValue) {
        return client.getVariant(toggleName, defaultValue);
    }

    @Override
    @Deprecated()
    public List<String> getFeatureToggleNames() {
        return client.getFeatureToggleNames();
    }

    @Override
    public MoreOperations more() {
        return client.more();
    }
}
