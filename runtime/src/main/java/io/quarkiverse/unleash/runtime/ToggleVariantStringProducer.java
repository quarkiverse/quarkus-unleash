package io.quarkiverse.unleash.runtime;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

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
