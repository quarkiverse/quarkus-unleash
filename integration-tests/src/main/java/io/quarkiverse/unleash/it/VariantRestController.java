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
import io.quarkus.runtime.annotations.RegisterForReflection;

@Path("variant")
@Produces(MediaType.APPLICATION_JSON)
public class VariantRestController {

    @FeatureVariant(name = "toggle")
    Instance<Variant> variant;

    @FeatureVariant(name = "toggle")
    Instance<Variant> notFound;

    @FeatureVariant(name = "toggle")
    Instance<Variant> found;

    @FeatureVariant(name = "toggle")
    Instance<String> defaultType;

    @FeatureVariant(name = "toggle")
    Instance<Param> jsonType;

    @FeatureVariant(name = "toggle")
    Instance<io.quarkiverse.unleash.it.Param> jsonType2;

    @Inject
    Unleash unleash;

    @GET
    public Response variants() {
        Map<String, Object> tmp = new HashMap<>();
        tmp.put("defaultType", Map.of("data", defaultType.get()));
        tmp.put("jsonType", jsonType.get());
        tmp.put("i-toggle", variant.get());
        tmp.put("notFound", notFound.get());
        tmp.put("found", found.get());
        tmp.put("toggle", unleash.getVariant("toggle", io.getunleash.Variant.DISABLED_VARIANT));
        return Response.ok(tmp).build();
    }

    @RegisterForReflection
    public static class Param {
        public String text;

        public Long value;

        public Boolean enabled;

    }
}
