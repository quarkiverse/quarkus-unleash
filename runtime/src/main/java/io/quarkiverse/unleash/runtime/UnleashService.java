package io.quarkiverse.unleash.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;

import io.getunleash.Unleash;
import io.quarkus.runtime.ApplicationConfig;
import io.quarkus.runtime.ShutdownEvent;

@ApplicationScoped
public class UnleashService {

    Unleash client;

    void initialize(UnleashRuntimeTimeConfig config, ApplicationConfig appConfig) {
        client = UnleashCreator
                .createUnleash(config,
                        config.appName.orElse(appConfig.name.orElse("default-app-name")));
    }

    void onStop(@Observes ShutdownEvent event) {
        client.shutdown();
    }

    @Produces
    public Unleash client() {
        return client;
    }

}
