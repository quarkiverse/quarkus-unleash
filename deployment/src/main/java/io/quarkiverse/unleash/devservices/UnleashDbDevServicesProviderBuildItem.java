package io.quarkiverse.unleash.devservices;

import io.quarkus.builder.item.SimpleBuildItem;

public final class UnleashDbDevServicesProviderBuildItem extends SimpleBuildItem {

    public String host;
    public String username;
    public String password;

    public String name;

    public int port;

    public UnleashDbDevServicesProviderBuildItem(String host, int port, String name, String username, String password) {
        this.host = host;
        this.port = port;
        this.name = name;
        this.username = username;
        this.password = password;
    }

}
