package io.quarkiverse.unleash.runtime;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;

public class UnleashCreator {

    public static Unleash createUnleash(UnleashRuntimeTimeConfig unleashRuntimeTimeConfig, String name) {
        UnleashConfig.Builder builder = UnleashConfig.builder()
                .unleashAPI(unleashRuntimeTimeConfig.url)
                .appName(name);

        unleashRuntimeTimeConfig.instanceId.ifPresent(builder::instanceId);
        unleashRuntimeTimeConfig.token.ifPresent(s -> builder.customHttpHeader("Authorization", s));
        unleashRuntimeTimeConfig.environment.ifPresent(builder::environment);
        unleashRuntimeTimeConfig.projectName.ifPresent(builder::projectName);
        unleashRuntimeTimeConfig.backupFile.ifPresent(builder::backupFile);

        builder.fetchTogglesInterval(unleashRuntimeTimeConfig.fetchTogglesInterval);
        builder.sendMetricsInterval(unleashRuntimeTimeConfig.sendMetricsInterval);
        builder.synchronousFetchOnInitialisation(unleashRuntimeTimeConfig.synchronousFetchOnInitialisation);

        if (unleashRuntimeTimeConfig.enableProxyAuthenticationByJvmProperties) {
            builder.enableProxyAuthenticationByJvmProperties();
        }
        if (unleashRuntimeTimeConfig.disableMetrics) {
            builder.disableMetrics();
        }

        return new DefaultUnleash(builder.build());
    }
}
