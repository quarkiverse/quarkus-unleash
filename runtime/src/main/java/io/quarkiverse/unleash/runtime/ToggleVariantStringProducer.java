package io.quarkiverse.unleash.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import io.getunleash.Unleash;
import io.quarkiverse.unleash.FeatureVariant;

@Singleton
public class ToggleVariantStringProducer extends AbstractVariantProducer {

    @Inject
    Unleash unleash;

    @Produces
    @FeatureVariant(name = "ignored")
    String variantProducer(InjectionPoint injectionPoint) {
        return getVariantString(injectionPoint, unleash);
    }
}
