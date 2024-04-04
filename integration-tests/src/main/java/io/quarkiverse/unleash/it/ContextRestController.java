package io.quarkiverse.unleash.it;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.context.*;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import io.getunleash.*;
import io.quarkiverse.unleash.FeatureToggle;
import io.quarkiverse.unleash.runtime.UnleashContextProvider;

@Path("context")
@Produces(MediaType.APPLICATION_JSON)
public class ContextRestController {

    @FeatureToggle(name = "rollout")
    Instance<Boolean> feature;

    @Inject
    UnleashContextProvider contextProvider;

    @Inject
    UnleashContext context;

    @GET
    public Response testMethod() {
        CompletableFuture<Map<String, Object>> appFuture = new CompletableFuture<>();
        new Thread(() -> appFuture.complete(getApplicationFlags())).start();

        Map<String, Object> tmp = appFuture.join();
        tmp.put("request", feature.get());
        tmp.put("request-environment", context.getEnvironment().orElse(null));
        tmp.put("request-userId", context.getUserId().orElse(null));
        return Response.ok(tmp).build();
    }

    private Map<String, Object> getApplicationFlags() {
        UnleashContext appContext = contextProvider.get();
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("application", feature.get());
        tmp.put("application-environment", appContext.getEnvironment().orElse(null));
        tmp.put("application-userId", appContext.getUserId().orElse(null));
        return tmp;
    }
}
