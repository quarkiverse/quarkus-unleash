package io.quarkiverse.unleash.runtime;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Shutdown;
import jakarta.enterprise.event.Startup;

import io.getunleash.Unleash;
import io.quarkus.logging.Log;

public class UnleashLifecycleManager {

    // This method is used to eagerly create the Unleash bean instance at RUNTIME_INIT execution time.
    void onStartup(@Observes Startup event, Unleash unleash) {
        unleash.more();
    }

    void onShutdown(@Observes Shutdown event, Unleash unleash) {
        try {
            unleash.shutdown();
        } catch (Exception e) {
            Log.error("The shutdown of the Unleash client failed", e);
        }
    }
}
