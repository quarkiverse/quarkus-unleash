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

import io.getunleash.Unleash;
import io.getunleash.Variant;
import io.quarkiverse.unleash.FeatureVariant;

@Path("variant")
@Produces(MediaType.APPLICATION_JSON)
public class VariantRestController {

    @Inject
    @FeatureVariant(toggleName = "toggle")
    Instance<Variant> variant;

    @Inject
    Unleash unleash;

    @GET
    public Response variants() {
        Map<String, Variant> tmp = new HashMap<>();
        tmp.put("i-toggle", variant.get());
        tmp.put("toggle", unleash.getVariant("toggle", io.getunleash.Variant.DISABLED_VARIANT));
        return Response.ok(tmp).build();
    }
}
