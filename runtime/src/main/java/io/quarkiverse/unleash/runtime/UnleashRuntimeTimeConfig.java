package io.quarkiverse.unleash.runtime;

import java.util.List;
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
     * Disable Unleash polling. If used without `synchronous-fetch-on-initialisation`, will cause the client to never fetch
     * toggles.
     */
    @WithName("disable-polling")
    @WithDefault("false")
    boolean disablePolling();

    /**
     * If provided, the Unleash client will only fetch toggles whose name starts with the provided value.
     */
    Optional<String> namePrefix();

    /**
     * Custom HTTP headers to be sent with requests to Unleash server.
     * Both header name and value can be static or come from environment variables.
     * Example: quarkus.unleash.custom-headers[0].name=X-Api-Key
     * quarkus.unleash.custom-headers[0].value=${SECRET}
     */
    @WithName("custom-headers")
    List<CustomHeader> customHeaders();

    /**
     * Custom header configuration
     */
    interface CustomHeader {
        /**
         * The name of the custom HTTP header.
         * Example: X-Api-Key or ${HEADER_NAME} to use an environment variable
         */
        String name();

        /**
         * The value of the custom HTTP header.
         * Example: secret-value or ${HEADER_VALUE} to use an environment variable
         */
        String value();
    }
}
