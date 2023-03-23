package io.quarkiverse.unleash.it;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.quarkiverse.unleash.FeatureToggle;

@Path("tests-anno")
@Produces(MediaType.APPLICATION_JSON)
public class TestAnnoRestController {

    @Inject
    @FeatureToggle(name = "quarkus-unleash-test-enabled")
    Instance<Boolean> enabled;

    @Inject
    @FeatureToggle(name = "quarkus-unleash-test-disabled")
    Instance<Boolean> disabled;

    @Inject
    @FeatureToggle(name = "toggle")
    Instance<Boolean> toggle;

    @Inject
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
