package io.quarkiverse.unleash;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.getunleash.Unleash;
import io.quarkiverse.unleash.runtime.UnleashCreator;
import io.quarkiverse.unleash.runtime.UnleashRuntimeTimeConfig;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class UnleashTestResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private static final Logger log = LoggerFactory.getLogger(UnleashTestResource.class);

    static UnleashAdmin ADMIN;

    static Unleash CLIENT;

    @Override
    public Map<String, String> start() {
        return null;
    }

    @Override
    public void stop() {
        try {
            CLIENT.shutdown();
        } catch (Exception ex) {
            // ignore
        }
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(ADMIN,
                new TestInjector.AnnotatedAndMatchesType(InjectUnleashAdmin.class, UnleashAdmin.class));
        testInjector.injectIntoFields(CLIENT,
                new TestInjector.AnnotatedAndMatchesType(InjectUnleash.class, Unleash.class));
    }

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        String host = context.devServicesProperties().get("quarkiverse.unleash.devservices.admin.url");
        if (host != null) {
            // admin client
            String url = String.format("http://%s/api", host);
            ADMIN = new UnleashAdmin(url);

            // unleash client
            Config appConfig = ConfigProvider.getConfig();
            UnleashRuntimeTimeConfig configImpl = new UnleashRuntimeTimeConfig() {
                @Override
                public boolean active() {
                    return false;
                }

                @Override
                public String url() {
                    return url;
                }

                @Override
                public Optional<String> appName() {
                    return Optional.of("quarkus-unleash-test");
                }

                @Override
                public Optional<String> projectName() {
                    return appConfig.getOptionalValue("quarkus.unleash.project", String.class);
                }

                @Override
                public Optional<String> instanceId() {
                    return Optional.of("quarkus-unleash-test-" + UUID.randomUUID());
                }

                @Override
                public boolean disableMetrics() {
                    return true;
                }

                @Override
                public Optional<String> token() {
                    return Optional.empty();
                }

                @Override
                public Optional<String> environment() {
                    return Optional.empty();
                }

                @Override
                public long fetchTogglesInterval() {
                    return appConfig
                            .getOptionalValue("quarkus.unleash.fetch-toggles-interval", Long.class)
                            .orElse(2L);
                }

                @Override
                public long sendMetricsInterval() {
                    return 606066066666666666L;
                }

                @Override
                public Optional<String> backupFile() {
                    return Optional.empty();
                }

                @Override
                public boolean synchronousFetchOnInitialisation() {
                    return true;
                }

                @Override
                public boolean enableProxyAuthenticationByJvmProperties() {
                    return false;
                }

                @Override
                public Optional<String> namePrefix() {
                    return Optional.empty();
                }
            };

            CLIENT = UnleashCreator.createUnleash(configImpl, "quarkus-unleash-test");
            log.info("Unleash test client fetch feature toggle names: {}", CLIENT.more().getFeatureToggleNames());
        }
    }

}
