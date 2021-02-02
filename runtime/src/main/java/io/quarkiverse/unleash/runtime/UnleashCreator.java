package io.quarkiverse.unleash.runtime;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;

public class UnleashCreator {

    private final UnleashRuntimeTimeConfig unleashRuntimeTimeConfig;

    public UnleashCreator(UnleashRuntimeTimeConfig unleashRuntimeTimeConfig) {
        this.unleashRuntimeTimeConfig = unleashRuntimeTimeConfig;
    }

    public Unleash createUnleash() {
        UnleashConfig.Builder builder = UnleashConfig.builder()
                .unleashAPI(unleashRuntimeTimeConfig.url)
                .appName(unleashRuntimeTimeConfig.appName);

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
