package io.quarkiverse.unleash.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;

import io.getunleash.Variant;
import io.quarkiverse.unleash.FeatureToggle;
import io.quarkiverse.unleash.FeatureVariant;

@ApplicationScoped
public class Bean {

    Instance<Boolean> toggle;
    Instance<Variant> variant;

    public Bean(@FeatureToggle(name = "toggle") Instance<Boolean> toggle,
            @FeatureVariant(name = "toggle") Instance<Variant> variant) {
        this.toggle = toggle;
        this.variant = variant;
    }

    public Instance<Boolean> getToggle() {
        return toggle;
    }

    public Instance<Variant> getVariant() {
        return variant;
    }
}
