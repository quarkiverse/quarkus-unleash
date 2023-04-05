package io.quarkiverse.unleash.runtime;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.ApplicationConfig;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class UnleashRecorder {

    public void initializeProducers(UnleashRuntimeTimeConfig config, ApplicationConfig appConfig, boolean devMode) {
        UnleashService producer = Arc.container().instance(UnleashService.class).get();
        producer.initialize(config, appConfig, devMode);
    }

}
