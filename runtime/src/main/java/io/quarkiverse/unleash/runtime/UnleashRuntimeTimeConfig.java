package io.quarkiverse.unleash.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "unleash", phase = ConfigPhase.RUN_TIME)
public class UnleashRuntimeTimeConfig {

    /**
     * Unleash URL service endpoint
     */
    @ConfigItem(name = "url")
    public String url;

    /**
     * Application name
     */
    @ConfigItem(name = "application")
    public Optional<String> appName = Optional.empty();

    /**
     * Project name
     */
    @ConfigItem(name = "project")
    public Optional<String> projectName = Optional.empty();

    /**
     * Instance ID.
     */
    @ConfigItem(name = "instance-id")
    public Optional<String> instanceId = Optional.empty();

    /**
     * Disable Unleash metrics
     */
    @ConfigItem(name = "disable-metrics", defaultValue = "false")
    public boolean disableMetrics = false;

    /**
     * Application Unleash token
     */
    @ConfigItem(name = "token")
    public Optional<String> token = Optional.empty();

    /**
     * Application environment
     */
    @ConfigItem(name = "environment")
    public Optional<String> environment = Optional.empty();

    /**
     * Fetch toggles interval (in seconds)
     */
    @ConfigItem(name = "fetch-toggles-interval", defaultValue = "10")
    public long fetchTogglesInterval = 10;

    /**
     * Send metrics interval (in seconds)
     */
    @ConfigItem(name = "send-metrics-interval", defaultValue = "60")
    public long sendMetricsInterval = 60;

    /**
     * Backup file
     */
    @ConfigItem(name = "backup-file")
    public Optional<String> backupFile = Optional.empty();

    /**
     * A synchronous fetch on initialisation
     */
    @ConfigItem(name = "synchronous-fetch-on-initialisation", defaultValue = "false")
    public boolean synchronousFetchOnInitialisation = false;

    /**
     * Enable proxy authentication by JVM properties
     */
    @ConfigItem(name = "enable-proxy-authentication-by-jvm-properties", defaultValue = "false")
    public boolean enableProxyAuthenticationByJvmProperties = false;
}
