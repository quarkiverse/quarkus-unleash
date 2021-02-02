package io.quarkiverse.unleash.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "unleash", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class UnleashRuntimeTimeConfig {

    /**
     * Unleash URL service endpoint
     */
    @ConfigItem
    public String url;

    /**
     * Application name
     */
    @ConfigItem
    public String appName;

    /**
     * Instance ID.
     */
    @ConfigItem
    public Optional<String> instanceId = Optional.empty();

    /**
     * Disable Unleash metrics
     */
    @ConfigItem(defaultValue = "false")
    public boolean disableMetrics = false;

    /**
     * Application Unleash token
     */
    @ConfigItem
    public Optional<String> token = Optional.empty();

    /**
     * Application environment
     */
    @ConfigItem
    public Optional<String> environment = Optional.empty();

    /**
     * Project name
     */
    @ConfigItem
    public Optional<String> projectName = Optional.empty();

    /**
     * Fetch toggles interval
     */
    @ConfigItem(defaultValue = "10")
    public long fetchTogglesInterval = 10;

    /**
     * Send metrics interval
     */
    @ConfigItem(defaultValue = "60")
    public long sendMetricsInterval = 60;

    /**
     * Backup file
     */
    @ConfigItem
    public Optional<String> backupFile = Optional.empty();

    /**
     * A synchronous fetch on initialisation
     */
    @ConfigItem(defaultValue = "false")
    public boolean synchronousFetchOnInitialisation = false;

    /**
     * Enable proxy authentication by JVM properties
     */
    @ConfigItem(defaultValue = "false")
    public boolean enableProxyAuthenticationByJvmProperties = false;
}
