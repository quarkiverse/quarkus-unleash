package io.quarkiverse.unleash.runtime;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.*;
import jakarta.inject.Inject;

import io.getunleash.UnleashContext;

public class UnleashContextProducer {

    @Inject
    QuarkusUnleashContextProvider provider;

    private List<UnleashContextCustomizer> requestCustomizers;

    @Inject
    void setRequestCustomizers(Instance<UnleashContextCustomizer> customizers) {
        requestCustomizers = customizers.stream()
                .filter(c -> !c.isApplicationContext())
                .toList();
    }

    @Produces
    @RequestScoped
    public UnleashContext getRequestContext() {
        if (requestCustomizers.isEmpty()) {
            return provider.getApplicationContext();
        }
        UnleashContext.Builder builder = new UnleashContext.Builder(provider.getApplicationContext());
        requestCustomizers.forEach(c -> c.apply(builder));
        return builder.build();
    }
}
