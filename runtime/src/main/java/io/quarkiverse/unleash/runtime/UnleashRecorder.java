package io.quarkiverse.unleash.runtime;

import java.util.function.Supplier;

import org.jboss.logging.Logger;

import io.getunleash.Unleash;
import io.quarkus.runtime.ApplicationConfig;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class UnleashRecorder {

    private static final Logger LOGGER = Logger.getLogger(UnleashRecorder.class);

    private final ApplicationConfig applicationConfig;
    private final RuntimeValue<UnleashRuntimeTimeConfig> unleashRuntimeConfig;

    public UnleashRecorder(ApplicationConfig applicationConfig, RuntimeValue<UnleashRuntimeTimeConfig> unleashRuntimeConfig) {
        this.applicationConfig = applicationConfig;
        this.unleashRuntimeConfig = unleashRuntimeConfig;
    }

    public Supplier<Unleash> getSupplier() {
        if (unleashRuntimeConfig.getValue().active) {
            String app = unleashRuntimeConfig.getValue().appName.orElse(applicationConfig.name.orElse("default-app-name"));
            return new Supplier<Unleash>() {
                @Override
                public Unleash get() {
                    Unleash unleash = UnleashCreator.createUnleash(unleashRuntimeConfig.getValue(), app);
                    LOGGER.infof("Unleash client application '{}' fetch feature toggle names: {}", app,
                            unleash.more().getFeatureToggleNames());
                    return unleash;
                }
            };
        } else {
            return new Supplier<Unleash>() {
                @Override
                public Unleash get() {
                    LOGGER.info("Unleash extension disabled from configuration");
                    return new NoOpUnleash();
                }
            };
        }
    }
}
