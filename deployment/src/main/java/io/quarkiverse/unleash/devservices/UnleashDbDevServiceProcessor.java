package io.quarkiverse.unleash.devservices;

import static io.quarkiverse.unleash.UnleashProcessor.FEATURE_NAME;
import static io.quarkiverse.unleash.devservices.UnleashDevServiceProcessor.PROP_UNLEASH_URL;
import static io.quarkus.runtime.LaunchMode.DEVELOPMENT;

import java.io.Closeable;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.unleash.UnleashBuildTimeConfig;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;

public class UnleashDbDevServiceProcessor {

    private static final Logger log = Logger.getLogger(UnleashDbDevServiceProcessor.class);

    static volatile UnleashDbRunningDevService devService;

    static volatile UnleashDbDevServiceCfg cfg;

    static volatile boolean first = true;

    public static final String DB_FEATURE_NAME = FEATURE_NAME + "-db";
    private static final String DB_ALIAS = DB_FEATURE_NAME;

    private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-unleash-db";
    public static final int DEFAULT_UNLEASH_PORT = 5432;

    private static final ContainerLocator unleashContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL,
            DEFAULT_UNLEASH_PORT);

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { GlobalDevServicesConfig.Enabled.class })
    public DevServicesResultBuildItem startUnleashDbContainers(LaunchModeBuildItem launchMode,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            UnleashBuildTimeConfig buildTimeConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            BuildProducer<UnleashDbDevServicesProviderBuildItem> startResultProducer,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            DockerStatusBuildItem dockerStatusBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem, GlobalDevServicesConfig devServicesConfig) {

        UnleashDbDevServiceCfg configuration = getConfiguration(buildTimeConfig);

        if (devService != null) {
            boolean shouldShutdownTheBroker = !configuration.equals(cfg);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            stopContainer();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Unleash Database Dev Services Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);
        try {
            devService = startContainer(dockerStatusBuildItem, configuration, launchMode,
                    !devServicesSharedNetworkBuildItem.isEmpty(),
                    devServicesConfig.timeout);
            if (devService == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (devService == null) {
            return null;
        }

        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    stopContainer();
                }
                first = true;
                devService = null;
                cfg = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        cfg = configuration;

        if (devService.isOwner()) {

            log.infof("The unleash database is ready to accept connections. Config: %s:%S/%s",
                    devService.dbHost,
                    devService.dbPort,
                    devService.dbName);

            startResultProducer.produce(new UnleashDbDevServicesProviderBuildItem(
                    devService.dbHost,
                    devService.dbPort,
                    devService.dbName,
                    devService.dbUsername,
                    devService.dbPassword));
        }

        return devService.toBuildItem();
    }

    private UnleashDbRunningDevService startContainer(DockerStatusBuildItem dockerStatusBuildItem,
            UnleashDbDevServiceCfg config,
            LaunchModeBuildItem launchMode, boolean useSharedNetwork, Optional<Duration> timeout) {

        if (!config.devServicesEnabled) {
            // explicitly disabled
            log.debug("Not starting dev services for Zeebe as it has been disabled in the config");
            return null;
        }

        if (!dockerStatusBuildItem.isDockerAvailable()) {
            log.warn(
                    "Docker isn't working, please configure the unleash URL property ("
                            + PROP_UNLEASH_URL + ").");
            return null;
        }

        final Optional<ContainerAddress> maybeContainerAddress = unleashContainerLocator.locateContainer(config.serviceName,
                config.shared,
                launchMode.getLaunchMode());

        // Starting the broker
        final Supplier<UnleashDbRunningDevService> defaultUnleashSupplier = () -> {

            UnleashPostgreSQLContainer container = new UnleashPostgreSQLContainer(
                    config.imageName,
                    launchMode.getLaunchMode() == DEVELOPMENT ? config.serviceName : null,
                    useSharedNetwork);

            timeout.ifPresent(container::withStartupTimeout);

            // enable test-container reuse
            if (config.reuse) {
                container.withReuse(true);
            }

            container.start();

            return new UnleashDbRunningDevService(DB_FEATURE_NAME, container.getContainerId(),
                    new ContainerShutdownCloseable(container, DB_FEATURE_NAME),
                    container.getUnleashDbHost(),
                    container.getUnleashDbPort(),
                    container.getDatabaseName(),
                    container.getUsername(),
                    container.getPassword());
        };

        return maybeContainerAddress
                .map(containerAddress -> new UnleashDbRunningDevService(DB_FEATURE_NAME, containerAddress.getId(), null))
                .orElseGet(defaultUnleashSupplier);

    }

    private UnleashDbDevServiceCfg getConfiguration(UnleashBuildTimeConfig cfg) {
        UnleashDevServicesConfig devServicesConfig = cfg.devService;
        return new UnleashDbDevServiceCfg(devServicesConfig);
    }

    private static final class UnleashDbDevServiceCfg {

        private final boolean devServicesEnabled;
        private final String imageName;
        private final boolean shared;
        private final String serviceName;

        private final boolean reuse;

        public UnleashDbDevServiceCfg(UnleashDevServicesConfig config) {
            this.devServicesEnabled = config.enabled;
            UnleashDevServicesConfig.UnleashDatabaseConfig tmp = config.db;
            this.imageName = tmp.imageName;
            this.serviceName = tmp.serviceName;
            this.shared = config.shared;
            this.reuse = config.reuse;

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UnleashDbDevServiceCfg that = (UnleashDbDevServiceCfg) o;
            return devServicesEnabled == that.devServicesEnabled && Objects.equals(imageName, that.imageName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(devServicesEnabled, imageName);
        }
    }

    private static class UnleashPostgreSQLContainer extends PostgreSQLContainer<UnleashPostgreSQLContainer> {
        private final boolean useSharedNetwork;
        private String hostName = null;

        public UnleashPostgreSQLContainer(String imageName, String serviceName,
                boolean useSharedNetwork) {
            super(DockerImageName
                    .parse(imageName)
                    .asCompatibleSubstituteFor(DockerImageName.parse(PostgreSQLContainer.IMAGE)));

            log.debugf("Unleash database docker image %s", imageName);

            this.useSharedNetwork = useSharedNetwork;
            if (serviceName != null) {
                this.withLabel(DEV_SERVICE_LABEL, serviceName);
            }

            addExposedPort(POSTGRESQL_PORT);
            hostName = DB_ALIAS;
            this.withNetworkAliases(DB_ALIAS);
        }

        @Override
        protected void configure() {
            super.configure();
            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, DB_ALIAS);
            } else {
                withNetwork(Network.SHARED);
            }
        }

        public String getUnleashDbHost() {
            return hostName;
        }

        public Integer getUnleashDbPort() {
            return POSTGRESQL_PORT;
        }

    }

    public static class UnleashDbRunningDevService extends DevServicesResultBuildItem.RunningDevService {

        public String dbHost;
        public String dbUsername;
        public String dbPassword;
        public String dbName;

        public int dbPort;

        public UnleashDbRunningDevService(String name, String containerId, Closeable closeable) {
            super(name, containerId, closeable, Map.of());
        }

        public UnleashDbRunningDevService(String name, String containerId, Closeable closeable, String dbHost, int dbPort,
                String dbName, String dbUsername, String dbPassword) {
            super(name, containerId, closeable, Map.of());
            this.dbHost = dbHost;
            this.dbPort = dbPort;
            this.dbName = dbName;
            this.dbUsername = dbUsername;
            this.dbPassword = dbPassword;
        }
    }

    private void stopContainer() {
        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop the Zeebe broker", e);
            } finally {
                devService = null;
            }
        }
    }
}
