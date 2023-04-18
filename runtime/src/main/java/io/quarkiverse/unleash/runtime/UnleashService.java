package io.quarkiverse.unleash.runtime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.getunleash.Unleash;
import io.quarkus.logging.Log;
import io.quarkus.runtime.ApplicationConfig;
import io.quarkus.runtime.ShutdownEvent;

@ApplicationScoped
public class UnleashService {

    private static final Logger log = LoggerFactory.getLogger(UnleashService.class);

    static Unleash client;

    boolean devMode;

    void initialize(UnleashRuntimeTimeConfig config, ApplicationConfig appConfig, boolean devMode) {
        this.devMode = devMode;
        if (devMode && client != null) {
            return;
        }
        String app = config.appName.orElse(appConfig.name.orElse("default-app-name"));
        client = UnleashCreator.createUnleash(config, app);

        log.info("Unleash client application '{}' fetch feature toggle names: {}", app,
                client.more().getFeatureToggleNames());
    }

    void onStop(@Observes ShutdownEvent event) {
        if (devMode) {
            return;
        }
        try {
            client.shutdown();
        } catch (Exception ex) {
            Log.error("Shutdown unleash client failed!", ex);
        }
    }

    @Produces
    public Unleash client() {
        return client;
    }

}
