package io.quarkiverse.unleash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.getunleash.Unleash;
import io.getunleash.variant.Variant;
import io.quarkus.test.QuarkusUnitTest;

public class NoOpUnleashTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest().withApplicationRoot((jar) -> jar
            .addAsResource(new StringAsset("quarkus.unleash.active=false"), "application.properties")
            .addClass(TestBean.class));

    @Inject
    TestBean testBean;

    @Test
    void testFeatureToggle() {
        assertFalse(testBean.getAlpha().get());
        assertTrue(testBean.getBravo().get());
    }

    @Test
    void testFeatureVariant() {
        assertNull(testBean.getCharlie().get());
        assertNull(testBean.getDelta().get());
        assertNull(testBean.getEcho().get());
    }

    @Test
    void testUnleashValues() {
        assertFalse(testBean.isEnabled("foxtrot"));
        assertTrue(testBean.isEnabled("golf", true));
        assertNull(testBean.getVariant("hotel"));
        Variant variant = new Variant("india", "payload", true);
        assertEquals(variant, testBean.getVariant("india", variant));
    }

    @ApplicationScoped
    static class TestBean {

        @FeatureToggle(name = "alpha")
        Instance<Boolean> alpha;

        @FeatureToggle(name = "bravo", defaultValue = true)
        Instance<Boolean> bravo;

        @FeatureVariant(name = "charlie")
        Instance<String> charlie;

        @FeatureVariant(name = "delta")
        Instance<Variant> delta;

        @FeatureVariant(name = "echo")
        Instance<Param> echo;

        @Inject
        Unleash unleash;

        public Instance<Boolean> getAlpha() {
            return alpha;
        }

        public Instance<Boolean> getBravo() {
            return bravo;
        }

        public Instance<String> getCharlie() {
            return charlie;
        }

        public Instance<Variant> getDelta() {
            return delta;
        }

        public Instance<Param> getEcho() {
            return echo;
        }

        public Unleash getUnleash() {
            return unleash;
        }

        public boolean isEnabled(String toggleName) {
            return unleash.isEnabled(toggleName);
        }

        public boolean isEnabled(String toggleName, boolean defaultSetting) {
            return unleash.isEnabled(toggleName, defaultSetting);
        }

        public Variant getVariant(String toggleName) {
            return unleash.getVariant(toggleName);
        }

        public Variant getVariant(String toggleName, Variant defaultValue) {
            return unleash.getVariant(toggleName, defaultValue);
        }
    }

    public static class Param {
        public String text;
        public Long value;
        public Boolean enabled;
    }
}
