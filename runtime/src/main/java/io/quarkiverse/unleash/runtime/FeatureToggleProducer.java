package io.quarkiverse.unleash.runtime;

import java.lang.annotation.Annotation;

import jakarta.enterprise.inject.*;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.quarkiverse.unleash.FeatureToggle;

public class FeatureToggleProducer {

    private static final Logger LOGGER = Logger.getLogger(FeatureToggleProducer.class);

    @Inject
    Unleash unleash;

    @Inject
    UnleashContextProvider contextProvider;

    @Produces
    @FeatureToggle(name = "ignored")
    boolean getRequestFeatureToggle(InjectionPoint injectionPoint) {
        return getFeatureToggle(injectionPoint, contextProvider.get());
    }

    private boolean getFeatureToggle(InjectionPoint injectionPoint, UnleashContext context) {
        FeatureToggle ft = null;
        for (Annotation qualifier : injectionPoint.getQualifiers()) {
            if (qualifier.annotationType().equals(FeatureToggle.class)) {
                ft = (FeatureToggle) qualifier;
                break;
            }
        }
        boolean value = unleash.isEnabled(ft.name(), context, ft.defaultValue());
        logToggleResult(ft, value, context);
        return value;
    }

    private void logToggleResult(FeatureToggle ft, boolean value, UnleashContext context) {
        if (!LOGGER.isDebugEnabled())
            return;
        StringBuilder sb = new StringBuilder("Feature ")
                .append(ft.name())
                .append(" is ")
                .append(value)
                .append(" (");
        int length = sb.length();
        context.getAppName().ifPresent(v -> sb.append("appName=").append(v).append(", "));
        context.getEnvironment().ifPresent(v -> sb.append("environment=").append(v).append(", "));
        context.getUserId().ifPresent(v -> sb.append("userId=").append(v).append(", "));
        context.getSessionId().ifPresent(v -> sb.append("sessionId=").append(v).append(", "));
        context.getRemoteAddress().ifPresent(v -> sb.append("remoteAddress=").append(v).append(", "));
        context.getProperties().forEach((k, v) -> sb.append(k).append("=").append(v).append(", "));
        sb.setLength(sb.length() - 2);
        if (length < sb.length())
            sb.append(") ");
        LOGGER.debug(sb);
    }
}
