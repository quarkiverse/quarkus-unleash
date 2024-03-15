package io.quarkiverse.unleash.runtime;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.repository.ToggleBootstrapProvider;
import io.getunleash.util.UnleashConfig;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.InstanceHandle;

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

        /*
         * The Arc container will be null in tests when @InjectUnleash is used.
         * TODO Make the Unleash client managed by CDI when @InjectUnleash is used.
         */
        ArcContainer arcContainer = Arc.container();
        if (arcContainer != null) {

            InstanceHandle<UnleashSubscriber> unleashSubscriber = arcContainer.instance(UnleashSubscriber.class);
            if (unleashSubscriber.isAvailable()) {
                builder.subscriber(unleashSubscriber.get());
            }

            InstanceHandle<ToggleBootstrapProvider> toggleBootstrapProvider = arcContainer
                    .instance(ToggleBootstrapProvider.class);
            if (toggleBootstrapProvider.isAvailable()) {
                builder.toggleBootstrapProvider(toggleBootstrapProvider.get());
            }
        }

        return new DefaultUnleash(builder.build());
    }
}
