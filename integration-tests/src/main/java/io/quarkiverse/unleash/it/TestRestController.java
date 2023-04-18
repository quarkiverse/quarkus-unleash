package io.quarkiverse.unleash.it;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.getunleash.Unleash;

@Path("tests")
@Produces(MediaType.APPLICATION_JSON)
public class TestRestController {

    @Inject
    Unleash unleash;

    @GET
    public Response testMethod() {
        Map<String, Boolean> tmp = new HashMap<>();
        tmp.put("quarkus-unleash-test-enabled", unleash.isEnabled("quarkus-unleash-test-enabled"));
        tmp.put("quarkus-unleash-test-disabled", unleash.isEnabled("quarkus-unleash-test-disabled"));
        tmp.put("toggle", unleash.isEnabled("toggle"));
        tmp.put("default-true", unleash.isEnabled("default-true", true));
        return Response.ok(tmp).build();
    }

    @GET
    @Path("{name}")
    public Boolean get(@PathParam("name") String name) {
        return unleash.isEnabled(name, false);
    }
}
