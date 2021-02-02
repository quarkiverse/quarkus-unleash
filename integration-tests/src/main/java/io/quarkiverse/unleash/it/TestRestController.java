package io.quarkiverse.unleash.it;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import no.finn.unleash.Unleash;

@Path("tests")
public class TestRestController {

    @Inject
    Unleash unleash;

    @GET
    public Response test() {
        return Response.ok(unleash.isEnabled("quarkus.unleash.test")).build();
    }
}
