package io.quarkiverse.unleash;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.inject.Singleton;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.getunleash.Variant;
import io.quarkus.test.QuarkusUnitTest;

public class EmptyAnnotationNameExceptionTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .withApplicationRoot(
                    (jar) -> jar.addClasses(EmptyFeatureToggleNameField.class, EmptyFeatureToggleNameConstructorArgument.class,
                            EmptyFeatureVariantNameField.class, EmptyFeatureVariantNameConstructorArgument.class))
            .assertException(t -> {
                assertEquals(DeploymentException.class, t.getClass());
                assertEquals(4, t.getSuppressed().length);
                assertEmptyAnnotationNameException(t, EmptyFeatureToggleNameField.class);
                assertEmptyAnnotationNameException(t, EmptyFeatureToggleNameConstructorArgument.class);
                assertEmptyAnnotationNameException(t, EmptyFeatureVariantNameField.class);
                assertEmptyAnnotationNameException(t, EmptyFeatureVariantNameConstructorArgument.class);
            });

    private static void assertEmptyAnnotationNameException(Throwable t, Class<?> expectedClassName) {
        assertEquals(1, Arrays.stream(t.getSuppressed()).filter(throwable -> {
            if (throwable instanceof EmptyAnnotationNameException e) {
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
    static class EmptyFeatureToggleNameField {

        @FeatureToggle(name = "")
        Instance<Boolean> toggle;
    }

    @Singleton
    static class EmptyFeatureToggleNameConstructorArgument {

        public EmptyFeatureToggleNameConstructorArgument(@FeatureToggle(name = "") Instance<Boolean> toggle) {
        }
    }

    @RequestScoped
    static class EmptyFeatureVariantNameField {

        @FeatureVariant(name = "")
        Instance<Variant> variant;
    }

    @Dependent
    static class EmptyFeatureVariantNameConstructorArgument {

        public EmptyFeatureVariantNameConstructorArgument(@FeatureVariant(name = "") Instance<Variant> variant) {
        }
    }
}
