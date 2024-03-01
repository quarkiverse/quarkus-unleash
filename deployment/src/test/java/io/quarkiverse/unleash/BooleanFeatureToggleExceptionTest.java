package io.quarkiverse.unleash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class BooleanFeatureToggleExceptionTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar.addClasses(BooleanField.class, BooleanMethodArg.class))
            .assertException(t -> {
                assertEquals(DeploymentException.class, t.getClass());
                assertEquals(2, t.getSuppressed().length);
                assertBooleanFeatureToggleException(t, BooleanField.class);
                assertBooleanFeatureToggleException(t, BooleanMethodArg.class);
            });

    private static void assertBooleanFeatureToggleException(Throwable t, Class<?> expectedClassName) {
        assertEquals(1, Arrays.stream(t.getSuppressed()).filter(throwable -> {
            if (throwable instanceof BooleanFeatureToggleException e) {
                return expectedClassName.getName().equals(e.getClassInfo().name().toString());
            }
            return false;
        }).count());
    }

    @Test
    void shouldNotBeInvoked() {
        fail("A deployment exception should be thrown before this method is ever invoked");
    }

    @ApplicationScoped
    static class BooleanField {

        @FeatureToggle(name = "toggle")
        boolean toggle;
    }

    @Singleton
    static class BooleanMethodArg {

        void doSomething(@FeatureToggle(name = "toggle") boolean toggle) {
        }
    }
}
