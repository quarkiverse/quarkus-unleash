package io.quarkiverse.unleash.runtime;

import java.util.*;

import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.*;

import io.getunleash.UnleashContext;
import io.quarkus.arc.Arc;

@ApplicationScoped
public class UnleashContextProvider {

    private UnleashContext applicationContext;

    @Inject
    UnleashContext requestContext;

    @Inject
    void setCustomizers(Instance<UnleashContextCustomizer> customizers) {
        UnleashContext.Builder builder = UnleashContext.builder();
        customizers.handlesStream()
                .filter(h -> STATIC_SCOPES.contains(h.getBean().getScope()))
                .map(Instance.Handle::get)
                .filter(UnleashContextCustomizer::isApplicationContext)
                .forEach(c -> c.apply(builder));
        applicationContext = builder.build();
    }

    public UnleashContext getApplicationContext() {
        return applicationContext;
    }

    public UnleashContext getRequestContext() {
        return requestContext;
    }

    public UnleashContext get() {
        if (Arc.container().requestContext().isActive()) {
            return getRequestContext();
        } else {
            return getApplicationContext();
        }
    }

    static final Set<Class<?>> STATIC_SCOPES = Set.of(ApplicationScoped.class, Singleton.class, Dependent.class);
}
