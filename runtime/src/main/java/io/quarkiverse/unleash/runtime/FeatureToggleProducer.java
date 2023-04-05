package io.quarkiverse.unleash.runtime;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.getunleash.Unleash;
import io.quarkiverse.unleash.FeatureToggle;

@Singleton
public class FeatureToggleProducer {

    @Inject
    Unleash unleash;

    @Produces
    @FeatureToggle(name = "ignored")
    boolean getFeatureToggle(InjectionPoint injectionPoint) {
        FeatureToggle ft = null;
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(FeatureToggle.class)) {
                ft = (FeatureToggle) qualifier;
                break;
            }
        }
        if (ft == null || ft.name().isEmpty()) {
            throw new IllegalStateException("No feature toggle name specified");
        }
        return unleash.isEnabled(ft.name(), ft.defaultValue());
    }

}
