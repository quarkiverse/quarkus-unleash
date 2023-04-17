# Quarkus Unleash

[![Build](https://github.com/quarkiverse/quarkus-unleash/workflows/Build/badge.svg?branch=main)](https://github.com/quarkiverse/quarkus-unleash/actions?query=workflow%3ABuild)
[![License](https://img.shields.io/github/license/quarkiverse/quarkus-unleash.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Central](https://img.shields.io/maven-central/v/io.quarkiverse.unleash/quarkus-unleash-parent?color=green)](https://central.sonatype.com/artifact/io.quarkiverse.unleash/quarkus-unleash-parent)
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-2-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->


## Compatibility

Quarkus Unleash provides two different version streams, one compatible with Quarkus 2.x and the other compatible with Quarkus 3.x.

| Quarkus | Quarkus Unleash | Documentation                                                                                        |
|---------|-----------------|------------------------------------------------------------------------------------------------------|
| 2.x     | 0.x             | [Documentation](https://quarkiverse.github.io/quarkiverse-docs/quarkus-unleash/dev/index.html) |
| 3.x     | 1.x             | [Documentation](https://quarkiverse.github.io/quarkiverse-docs/quarkus-unleash/dev/index.html) |

Use the latest version of the corresponding stream, [the list of versions is available on Maven Central](https://central.sonatype.com/artifact/io.quarkiverse.unleash/quarkus-unleash-parent).

## Getting started

To use the extension, add the dependency to the target project:

```xml
<dependency>
  <groupId>io.quarkiverse.unleash</groupId>
  <artifactId>quarkus-unleash</artifactId>
</dependency>
```
### Unleash client

```java
@ApplicationScoped
public class TestService {

    @Inject
    Unleash unleash;

    public boolean isTest() {
        return unleash.isEnabled("quarkus.unleash.test");
    }
}
```

### @FeatureToggle

By using the `@FeatureToggle` annotation there is a shortcut to inject feature toggle.

```java
@ApplicationScoped
public class TestService {

    @FeatureToggle(name = "my-toggle")
    Instance<Boolean> myToggle;

    @FeatureToggle(name = "my-toggle", defaultValue = true)
    Instance<Boolean> myToggleDefault;
    
}
```

### @FeatureVariant

By using the `@FeatureVariant` annotation there is a shortcut to inject feature toggle
variant or the payload of the variant.

```java
@ApplicationScoped
public class TestService {

    @FeatureVariant(name = "toggle-variant")
    Instance<Variant> variant;

    @FeatureVariant(name = "toggle-payload")
    Instance<String> variant;

    @FeatureVariant(name = "toggle-variant-json")
    Instance<MyCustomJsonModel> variant;

}
```

## Configuration

```properties
# Unleash URL service endpoint [required]
quarkus.unleash.url=
# Application name (default quarkus.application.name) [optional]
quarkus.unleash.application=
# Project name [optional]
quarkus.unleash.project=
# Instance ID [optional]
quarkus.unleash.instance-id=
# Disable Unleash metrics [optional]
quarkus.unleash.disable-metrics=false
# Application Unleash token [optional]
quarkus.unleash.token=
# Application environment [optional]
quarkus.unleash.environment=
# Fetch toggles interval (in seconds) [optional]
quarkus.unleash.fetch-toggles-interval=10
# Send metrics interval (in seconds) [optional]
quarkus.unleash.send-metrics-interval=60
# Backup file [optional]
quarkus.unleash.backup-file=
# A synchronous fetch on initialisation [optional]
quarkus.unleash.synchronous-fetch-on-initialisation=false
# Enable proxy authentication by JVM properties [optional]
quarkus.unleash.enable-proxy-authentication-by-jvm-properties=false
```

## Dev-Services
Dev Services for Unleash is automatically enabled unless:
* `quarkus.unleash.devservices.enabled` is set to false
* `quarkus.unleash.url` is configured

Dev Service for Unleash relies on Docker to start the broker. If your environment does not support Docker, you will need
to start the broker manually, or connect to an already running broker. You can configure the broker address using
`quarkus.unleash.url`.

#### Configuration

```properties
# Disable or enable dev-services
quarkus.unleash.devservices.enabled=true|false
# Optional fixed port the dev service will listen to
quarkus.unleash.devservices.port=
# Indicates if the Unleash server managed by Quarkus Dev Services is shared.
quarkus.unleash.devservices.shared=true
# This property is used when shared is set to true
quarkus.unleash.devservices.service-name=unleash
# The container image name to use, for container based DevServices providers.
quarkus.unleash.devservices.image-name=
# To enable reuse test-containers
quarkus.unleash.devservices.reuse=false
# The import data from file during the start.
quarkus.unleash.devservices.import-file=
# Database default image 
quarkus.unleash.devservices.db.image-name=postgres
# Database service name
quarkus.unleash.devservices.db.service-name=unleash-db
```

## Testing
To use the test extension, add this dependency to the project:
```xml
<dependency>
    <groupId>io.quarkiverse.unleash</groupId>
    <artifactId>quarkus-unleash-test</artifactId>
    <version>{version}</version>
    <scope>test</scope>
</dependency>
```

`UnleashTestResource` creates an instance of admin and standard `Unleash` clients just for testing.
These instances are separate from the application instances.

```java
@QuarkusTest
@QuarkusTestResource(UnleashTestResource.class)
public class BaseTest {

    @InjectUnleashAdmin
    UnleashAdmin admin;

    @InjectUnleash
    Unleash client;

    @Test
    public void test() {
        
        admin.toggleOff("toggle-1");
        admin.toggleOn("toggle-2");

        // wait for client change
        await().atLeast(12, SECONDS)
                .pollInterval(4, SECONDS)
                .until(() -> client.isEnabled("toggle-2"));
        
        boolean toggleOn = client.isEnabled("toggle-2");
        
        // ... test my application
        
    }
}
```
## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center"><a href="https://www.lorislab.org"><img src="https://avatars.githubusercontent.com/u/828045?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Andrej Petras</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-unleash/commits?author=andrejpetras" title="Code">💻</a> <a href="#maintenance-andrejpetras" title="Maintenance">🚧</a></td>
      <td align="center"><a href="http://melloware.com"><img src="https://avatars.githubusercontent.com/u/4399574?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Melloware</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-unleash/commits?author=melloware" title="Documentation">📖</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification.
Contributions of any kind welcome!
