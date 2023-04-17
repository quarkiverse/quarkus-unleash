package io.quarkiverse.unleash;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER, METHOD })
public @interface FeatureVariant {

    @Nonbinding
    String name();

    FeatureVariant INSTANCE = new FeatureVariant() {
        @Override
        public String name() {
            return "ignored";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return FeatureVariant.class;
        }
    };

}
