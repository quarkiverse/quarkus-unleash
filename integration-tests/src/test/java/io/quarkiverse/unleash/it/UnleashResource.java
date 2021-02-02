package io.quarkiverse.unleash.it;

import java.util.Collections;
import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class UnleashResource implements QuarkusTestResourceLifecycleManager {

    int UNLEASH_PORT = 4242;

    Network network = Network.newNetwork();

    PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:12")
            .withNetwork(network)
            .withLogConsumer(ContainerLogger.create("postgres"))
            .withNetworkAliases("postgresql")
            .withDatabaseName("unleash")
            .withUsername("unleash")
            .withPassword("unleash");

    GenericContainer<?> unleash = new GenericContainer<>("unleashorg/unleash-server:3.10.1")
            .withNetwork(network)
            .withEnv("DATABASE_URL", "postgres://unleash:unleash@postgresql:5432/unleash")
            .withLogConsumer(ContainerLogger.create("unleash"))
            .withExposedPorts(UNLEASH_PORT)
            .waitingFor(Wait.forLogMessage(".*Unleash has started.*\\s", 1));

    @Override
    public Map<String, String> start() {
        db.start();
        unleash.start();
        return Collections.singletonMap("quarkus.unleash.url",
                "http://" + unleash.getContainerIpAddress() + ":" + unleash.getMappedPort(UNLEASH_PORT) + "/api");
    }

    @Override
    public void stop() {
        unleash.close();
        db.close();
    }
}
