package io.quarkiverse.unleash.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;

import io.getunleash.Unleash;
import io.quarkus.logging.Log;
import io.quarkus.runtime.ApplicationConfig;
import io.quarkus.runtime.ShutdownEvent;

@ApplicationScoped
public class UnleashService {

    static Unleash client;

    boolean devMode;

    void initialize(UnleashRuntimeTimeConfig config, ApplicationConfig appConfig, boolean devMode) {
        this.devMode = devMode;
        if (devMode && client != null) {
            return;
        }
        client = UnleashCreator
                .createUnleash(config,
                        config.appName.orElse(appConfig.name.orElse("default-app-name")));
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
