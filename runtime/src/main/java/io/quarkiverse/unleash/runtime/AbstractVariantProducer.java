package io.quarkiverse.unleash.runtime;

import java.lang.annotation.Annotation;
import java.util.Optional;

import jakarta.enterprise.inject.spi.InjectionPoint;

import io.getunleash.Unleash;
import io.getunleash.Variant;
import io.getunleash.variant.Payload;
import io.quarkiverse.unleash.FeatureVariant;
import io.quarkiverse.unleash.UnleashJsonMapper;
import io.quarkus.logging.Log;

public class AbstractVariantProducer {

    protected FeatureVariant getFeatureVariant(InjectionPoint injectionPoint) {
        FeatureVariant ft = null;
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(FeatureVariant.class)) {
                ft = (FeatureVariant) qualifier;
                break;
            }
        }
        if (ft == null || ft.name().isEmpty()) {
            throw new IllegalStateException("No feature toggle name of the variant specified");
        }
        return ft;
    }

    protected Variant getVariant(InjectionPoint injectionPoint, Unleash unleash) {
        FeatureVariant ft = getFeatureVariant(injectionPoint);
        return unleash.getVariant(ft.name());
    }

    protected String getVariantString(InjectionPoint injectionPoint, Unleash unleash) {
        Variant variant = getVariant(injectionPoint, unleash);
        if (!variant.isEnabled()) {
            return null;
        }
        Optional<Payload> payload = variant.getPayload();
        return payload.map(Payload::getValue).orElse(null);
    }

    protected Object getVariantJsonObject(InjectionPoint injectionPoint, Class<?> clazz, Unleash unleash,
            UnleashJsonMapper mapper) {
        FeatureVariant ft = getFeatureVariant(injectionPoint);
        Variant variant = unleash.getVariant(ft.name());

        if (!variant.isEnabled()) {
            return null;
        }
        Optional<Payload> tmp = variant.getPayload();
        if (tmp.isEmpty()) {
            return null;
        }
        Payload payload = tmp.get();
        if (!payload.getType().equals("json")) {
            Log.warnf("Feature toggle '%s' variant '%s' is not type of 'json'. Type: %s", ft.name(), variant.getName(),
                    payload.getType());
            return null;
        }
        String value = payload.getValue();
        if (value == null) {
            return null;
        }
        return mapper.fromJson(value, clazz);
    }

}
