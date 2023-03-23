package io.quarkiverse.unleash;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

@Qualifier
@Retention(RUNTIME)
@Target({ FIELD, PARAMETER, METHOD })
public @interface FeatureVariant {

    String NULL_PAYLOAD = "<null-payload>";

    @Nonbinding
    String toggleName();

    @Nonbinding
    String defaultName() default "disabled";

    @Nonbinding
    boolean defaultEnabled() default false;

    @Nonbinding
    String defaultPayload() default NULL_PAYLOAD;
}
