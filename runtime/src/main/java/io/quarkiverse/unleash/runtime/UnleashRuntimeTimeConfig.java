package io.quarkiverse.unleash.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.unleash")
public interface UnleashRuntimeTimeConfig {

    /**
     * Whether or not the Unleash extension is active.
     */
    @WithName("active")
    @WithDefault("true")
    boolean active();

    /**
     * Unleash URL service endpoint
     */
    @WithName("url")
    String url();

    /**
     * Application name
     */
    @WithName("application")
    Optional<String> appName();

    /**
     * Project name
     */

    @WithName("project")
    Optional<String> projectName();

    /**
     * Instance ID.
     */

    @WithName("instance-id")
    Optional<String> instanceId();

    /**
     * Disable Unleash metrics
     */
    @WithName("disable-metrics")
    @WithDefault("false")
    boolean disableMetrics();

    /**
     * Application Unleash token
     */
    @WithName("token")
    Optional<String> token();

    /**
     * Application environment
     */
    @WithName("environment")
    Optional<String> environment();

    /**
     * Fetch toggles interval (in seconds)
     */
    @WithName("fetch-toggles-interval")
    @WithDefault("10")
    long fetchTogglesInterval();

    /**
     * Send metrics interval (in seconds)
     */
    @WithName("send-metrics-interval")
    @WithDefault("60")
    long sendMetricsInterval();

    /**
     * Backup file
     */
    @WithName("backup-file")
    Optional<String> backupFile();

    /**
     * A synchronous fetch on initialisation
     */
    @WithName("synchronous-fetch-on-initialisation")
    @WithDefault("false")
    boolean synchronousFetchOnInitialisation();

    /**
     * Enable proxy authentication by JVM properties
     */
    @WithName("enable-proxy-authentication-by-jvm-properties")
    @WithDefault("false")
    boolean enableProxyAuthenticationByJvmProperties();

    /**
     * If provided, the Unleash client will only fetch toggles whose name starts with the provided value.
     */
    Optional<String> namePrefix();
}
