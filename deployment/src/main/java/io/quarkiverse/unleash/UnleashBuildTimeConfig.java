package io.quarkiverse.unleash;

import io.quarkiverse.unleash.devservices.UnleashDevServicesConfig;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "unleash", phase = ConfigPhase.BUILD_TIME)
public class UnleashBuildTimeConfig {

    /**
     * Default Dev services configuration.
     */
    @ConfigItem(name = "devservices")
    public UnleashDevServicesConfig devService;

}
