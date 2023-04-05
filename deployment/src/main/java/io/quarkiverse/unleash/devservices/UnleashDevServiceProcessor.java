package io.quarkiverse.unleash.devservices;

import static io.quarkiverse.unleash.UnleashProcessor.FEATURE_NAME;
import static io.quarkus.runtime.LaunchMode.DEVELOPMENT;

import java.io.Closeable;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.jboss.logging.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.unleash.UnleashBuildTimeConfig;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;

public class UnleashDevServiceProcessor {

    private static final Logger log = Logger.getLogger(UnleashDevServiceProcessor.class);

    private static final String DEFAULT_DOCKER_IMAGE = "unleashorg/unleash-server:4";
    private static final String IMPORT_FILE_PATH = "/tmp/unleash-import-file.yml";
    public static final String PROP_UNLEASH_URL = "quarkus.unleash.url";
    private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-unleash";
    public static final int DEFAULT_UNLEASH_PORT = 4242;
    private static final ContainerLocator unleashContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL,
            DEFAULT_UNLEASH_PORT);
    static volatile UnleashRunningDevService devService;
    static volatile UnleashDevServiceCfg cfg;
    static volatile boolean first = true;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = { GlobalDevServicesConfig.Enabled.class })
    public DevServicesResultBuildItem startUnleashContainers(LaunchModeBuildItem launchMode,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            UnleashBuildTimeConfig buildTimeConfig,
            UnleashDbDevServicesProviderBuildItem dbSettings,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            DockerStatusBuildItem dockerStatusBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem, GlobalDevServicesConfig devServicesConfig) {

        UnleashDevServiceCfg configuration = getConfiguration(buildTimeConfig);

        if (devService != null) {
            boolean shouldShutdownTheBroker = !configuration.equals(cfg);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            stopContainer();
            cfg = null;
        }
        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Unleash Dev Services Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);
        try {
            devService = startContainer(dockerStatusBuildItem, configuration, dbSettings, launchMode,
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
            String tmp = devService.getConfig().get(PROP_UNLEASH_URL);
            log.infof("The unleash is ready to accept connections on %s", tmp);
            log.infof("The unleash admin URL %s", tmp.replaceAll("api", ""));
        }

        return devService.toBuildItem();
    }

    private UnleashRunningDevService startContainer(DockerStatusBuildItem dockerStatusBuildItem,
            UnleashDevServiceCfg config, UnleashDbDevServicesProviderBuildItem dbSettings,
            LaunchModeBuildItem launchMode, boolean useSharedNetwork, Optional<Duration> timeout) {

        if (!config.devServicesEnabled) {
            // explicitly disabled
            log.debug("Not starting dev services for Unleash as it has been disabled in the config");
            return null;
        }

        if (ConfigUtils.isPropertyPresent(PROP_UNLEASH_URL)) {
            log.debug("Not starting dev services for Unleash as '" + PROP_UNLEASH_URL + "' have been provided");
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

        // Starting to unleash service
        final Supplier<UnleashRunningDevService> defaultUnleashSupplier = () -> {

            DockerImageName image = DockerImageName.parse(config.imageName);

            UnleashContainer container = new UnleashContainer(
                    image,
                    config.fixedExposedPort,
                    launchMode.getLaunchMode() == DEVELOPMENT ? config.serviceName : null,
                    useSharedNetwork,
                    config.importFile,
                    dbSettings);
            timeout.ifPresent(container::withStartupTimeout);

            //            container.withLogConsumer(ContainerLogger.create(config.serviceName));

            // enable test-container reuse
            if (config.reuse) {
                container.withReuse(true);
            }

            container.start();

            return new UnleashRunningDevService(FEATURE_NAME, container.getContainerId(),
                    new ContainerShutdownCloseable(container, FEATURE_NAME),
                    configMap(container.getUrl()));
        };

        return maybeContainerAddress
                .map(containerAddress -> new UnleashRunningDevService(FEATURE_NAME,
                        containerAddress.getId(),
                        null, configMap(containerAddress.getUrl())))
                .orElseGet(defaultUnleashSupplier);
    }

    private static Map<String, String> configMap(String url) {
        Map<String, String> config = new HashMap<>();
        config.put(PROP_UNLEASH_URL, url);
        return config;
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

    public static class UnleashRunningDevService extends DevServicesResultBuildItem.RunningDevService {

        public UnleashRunningDevService(String name, String containerId, Closeable closeable, Map<String, String> config) {
            super(name, containerId, closeable, config);
        }
    }

    private UnleashDevServiceCfg getConfiguration(UnleashBuildTimeConfig cfg) {
        UnleashDevServicesConfig devServicesConfig = cfg.devService;
        return new UnleashDevServiceCfg(devServicesConfig);
    }

    private static final class UnleashDevServiceCfg {

        private final boolean devServicesEnabled;
        private final String imageName;
        private final Integer fixedExposedPort;
        private final boolean shared;
        private final String serviceName;

        private final String importFile;

        private final boolean reuse;

        public UnleashDevServiceCfg(UnleashDevServicesConfig config) {
            this.devServicesEnabled = config.enabled;
            this.imageName = config.imageName.orElse(DEFAULT_DOCKER_IMAGE);
            this.fixedExposedPort = config.port.orElse(0);
            this.shared = config.shared;
            this.serviceName = config.serviceName;
            this.reuse = config.reuse;
            this.importFile = config.importFile.orElse(null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UnleashDevServiceCfg that = (UnleashDevServiceCfg) o;
            return devServicesEnabled == that.devServicesEnabled && Objects.equals(imageName, that.imageName)
                    && Objects.equals(fixedExposedPort, that.fixedExposedPort);
        }

        @Override
        public int hashCode() {
            return Objects.hash(devServicesEnabled, imageName, fixedExposedPort);
        }
    }

    private static class UnleashContainer extends GenericContainer<UnleashContainer> {

        private final boolean useSharedNetwork;
        private final int fixedExposedPort;

        private String hostName = null;

        public UnleashContainer(DockerImageName image, int fixedExposedPort, String serviceName,
                boolean useSharedNetwork, String importFile, UnleashDbDevServicesProviderBuildItem dbSettings) {
            super(image);
            log.debugf("Unleash docker image %s", image);
            this.fixedExposedPort = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;

            this.withExposedPorts(DEFAULT_UNLEASH_PORT);

            // set up the database for unleash service
            this.withEnv("DATABASE_HOST", dbSettings.host)
                    .withEnv("LOG_LEVEL", "info")
                    .withEnv("DATABASE_NAME", dbSettings.name)
                    .withEnv("DATABASE_USERNAME", dbSettings.username)
                    .withEnv("DATABASE_PASSWORD", dbSettings.password)
                    .withEnv("DATABASE_PORT", "" + dbSettings.port)
                    .withEnv("AUTH_TYPE", "none")
                    .withEnv("DATABASE_SSL", "false");

            if (serviceName != null) {
                this.withLabel(DEV_SERVICE_LABEL, serviceName);
            }

            // import data file
            if (importFile != null) {
                log.infof("Unleash startup import file: %s", importFile);
                this.withEnv("IMPORT_FILE", IMPORT_FILE_PATH);
                this.withFileSystemBind(importFile, IMPORT_FILE_PATH, BindMode.READ_ONLY);
            }

            // wait for start
            this.waitingFor(Wait.forHttp("/"));
        }

        @Override
        protected void configure() {
            super.configure();

            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, "unleash");
                return;
            } else {
                withNetwork(Network.SHARED);
            }

            if (fixedExposedPort > 0) {
                addFixedExposedPort(fixedExposedPort, DEFAULT_UNLEASH_PORT);
            } else {
                addExposedPort(DEFAULT_UNLEASH_PORT);
            }
        }

        public int getPort() {
            if (useSharedNetwork) {
                return DEFAULT_UNLEASH_PORT;
            }
            if (fixedExposedPort > 0) {
                return fixedExposedPort;
            }
            return super.getFirstMappedPort();
        }

        /**
         * Returns an address accessible from within the container's network for the given port.
         *
         * @param port the target port
         * @return internally accessible address for {@code port}
         */
        public String getInternalAddress(final int port) {
            return getInternalHost() + ":" + port;
        }

        /**
         * Returns a hostname which is accessible from a host that is within the same docker network as
         * this node. It will attempt to return the last added network alias it finds, and if there is
         * none, will return the container name. The network alias is preferable as it typically conveys
         * more meaning than container name, which is often randomly generated.
         *
         * @return the hostname of this node as visible from a host within the same docker network
         */
        @API(status = API.Status.EXPERIMENTAL)
        public String getInternalHost() {
            final GenericContainer<?> container = self();
            final List<String> aliases = container.getNetworkAliases();
            if (aliases.isEmpty()) {
                return container.getContainerInfo().getName();
            }

            return aliases.get(aliases.size() - 1);
        }

        public String getUnleashHost() {
            return useSharedNetwork ? hostName : super.getHost();
        }

        public String getUrl() {
            return String.format("http://%s:%d/api", this.getUnleashHost(), this.getPort());
        }
    }
}
