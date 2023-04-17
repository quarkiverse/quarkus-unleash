package io.quarkiverse.unleash.it;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkiverse.unleash.FeatureToggle;

@Path("tests-anno")
@Produces(MediaType.APPLICATION_JSON)
public class TestAnnoRestController {

    @FeatureToggle(name = "quarkus-unleash-test-enabled")
    Instance<Boolean> enabled;

    @FeatureToggle(name = "quarkus-unleash-test-disabled")
    Instance<Boolean> disabled;

    @FeatureToggle(name = "toggle")
    Instance<Boolean> toggle;

    @FeatureToggle(name = "default-true", defaultValue = true)
    Instance<Boolean> defaultTrue;

    @GET
    public Response testAnnotation() {
        Map<String, Boolean> tmp = new HashMap<>();
        tmp.put("quarkus-unleash-test-enabled", enabled.get());
        tmp.put("quarkus-unleash-test-disabled", disabled.get());
        tmp.put("toggle", toggle.get());
        tmp.put("default-true", defaultTrue.get());
        return Response.ok(tmp).build();
    }
}
