package io.quarkiverse.unleash;

import jakarta.enterprise.context.ApplicationScoped;

import io.getunleash.event.UnleashSubscriber;
import io.getunleash.metric.ClientRegistration;

@ApplicationScoped
public class ClientRegisteredUnleashSubscriber implements UnleashSubscriber {

    private boolean clientRegistered;

    @Override
    public void clientRegistered(ClientRegistration clientRegistration) {
        clientRegistered = true;
    }

    public boolean isClientRegistered() {
        return clientRegistered;
    }
}
