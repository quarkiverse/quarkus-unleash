package io.quarkiverse.unleash.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import io.getunleash.Unleash;
import io.getunleash.variant.Variant;
import io.quarkiverse.unleash.FeatureVariant;

public class ToggleVariantProducer extends AbstractVariantProducer {

    @Inject
    Unleash unleash;

    @Produces
    @FeatureVariant(name = "ignored")
    Variant variantProducer(InjectionPoint injectionPoint) {
        return getVariant(injectionPoint, unleash);
    }
}
