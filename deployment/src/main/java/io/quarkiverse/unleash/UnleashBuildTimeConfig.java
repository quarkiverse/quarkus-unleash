package io.quarkiverse.unleash;

import io.quarkiverse.unleash.devservices.UnleashDevServicesConfig;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "quarkus.unleash")
public interface UnleashBuildTimeConfig {

    /**
     * Default Dev services configuration.
     */
    @WithName("devservices")
    UnleashDevServicesConfig devService();

}
