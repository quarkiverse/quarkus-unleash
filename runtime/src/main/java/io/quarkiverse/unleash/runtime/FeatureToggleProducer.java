package io.quarkiverse.unleash.runtime;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.getunleash.Unleash;
import io.getunleash.Variant;
import io.quarkiverse.unleash.FeatureToggle;
import io.quarkiverse.unleash.FeatureVariant;

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

    @Produces
    @FeatureVariant(toggleName = "ignored")
    Variant getVariant(InjectionPoint injectionPoint) {
        FeatureVariant ft = null;
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(FeatureVariant.class)) {
                ft = (FeatureVariant) qualifier;
                break;
            }
        }
        if (ft == null || ft.toggleName().isEmpty()) {
            throw new IllegalStateException("No feature toggle name of the variant specified");
        }
        String payload = ft.defaultPayload();
        if (FeatureVariant.NULL_PAYLOAD.equals(payload)) {
            payload = null;
        }
        return unleash.getVariant(ft.toggleName(), new Variant(ft.defaultName(), payload, ft.defaultEnabled()));
    }
}
