package io.quarkiverse.unleash.it;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.ResteasyProviderFactory;

import io.getunleash.UnleashContext;
import io.quarkiverse.unleash.runtime.UnleashContextCustomizer;
import io.quarkus.logging.Log;

@RequestScoped
public class RequestContextCustomizer implements UnleashContextCustomizer {

    @Override
    public void apply(UnleashContext.Builder builder) {
        Log.info("Applying RequestContextCustomizer");
        UriInfo info = ResteasyProviderFactory.getInstance().getContextData(UriInfo.class);
        builder.userId(info.getQueryParameters().getFirst("userId"))
                .addProperty("betaEnabled", info.getQueryParameters().getFirst("betaEnabled"));
    }
}
