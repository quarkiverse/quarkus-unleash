package io.quarkiverse.unleash.runtime;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.getunleash.Unleash;
import io.getunleash.Variant;
import io.quarkiverse.unleash.FeatureVariant;

@Singleton
public class ToggleVariantProducer extends AbstractVariantProducer {

    @Inject
    Unleash unleash;

    @Produces
    @FeatureVariant(name = "ignored")
    Variant variantProducer(InjectionPoint injectionPoint) {
        return getVariant(injectionPoint, unleash);
    }
}
