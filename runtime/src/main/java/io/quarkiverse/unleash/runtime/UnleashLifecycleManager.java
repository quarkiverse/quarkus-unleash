package io.quarkiverse.unleash.runtime;

import static io.quarkus.runtime.LaunchMode.DEVELOPMENT;
import static io.quarkus.runtime.configuration.ProfileManager.getLaunchMode;

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
        if (getLaunchMode() == DEVELOPMENT) {
            /*
             * If the Unleash client is shut down when Quarkus is live reloaded, the underlying ScheduledThreadPoolExecutor
             * will be shut down and no longer accept new tasks after Quarkus is done restarting. We need to keep the
             * executor alive in dev mode so that it'll keep working after the live reload.
             */
            return;
        }
        try {
            unleash.shutdown();
        } catch (Exception e) {
            Log.error("The shutdown of the Unleash client failed", e);
        }
    }
}
