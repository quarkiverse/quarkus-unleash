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
            UnleashRuntimeTimeConfig config = new UnleashRuntimeTimeConfig();
            config.url = url;
            config.appName = Optional.of("quarkus-unleash-test");
            config.projectName = appConfig.getOptionalValue("quarkus.unleash.project", String.class);
            config.environment = appConfig.getOptionalValue("quarkus.unleash.environment", String.class);
            config.instanceId = Optional.of("quarkus-unleash-test-" + UUID.randomUUID());
            config.fetchTogglesInterval = appConfig
                    .getOptionalValue("quarkus.unleash.fetch-toggles-interval", Long.class)
                    .orElse(2L);
            config.disableMetrics = true;
            config.synchronousFetchOnInitialisation = true;
            CLIENT = UnleashCreator.createUnleash(config, "quarkus-unleash-test");
            log.info("Unleash test client fetch feature toggle names: {}", CLIENT.more().getFeatureToggleNames());
        }
    }

}
