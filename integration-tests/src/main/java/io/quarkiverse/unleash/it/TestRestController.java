package io.quarkiverse.unleash.it;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.getunleash.Unleash;

@Path("tests")
public class TestRestController {

    @Inject
    Unleash unleash;

    @GET
    public Response test() {
        if (!unleash.isEnabled("quarkus-unleash-test-enabled")) {
            return Response.serverError().entity("quarkus-unleash-test-enabled is disabled!").build();
        }
        if (unleash.isEnabled("quarkus-unleash-test-disabled")) {
            return Response.serverError().entity("quarkus-unleash-test-disabled is enabled!").build();
        }
        return Response.ok(unleash.isEnabled("quarkus.unleash.test")).build();
    }
}
