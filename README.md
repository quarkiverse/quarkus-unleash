# Quarkus Unleash

[![Build](https://github.com/quarkiverse/quarkus-unleash/workflows/Build/badge.svg?branch=main)](https://github.com/quarkiverse/quarkus-unleash/actions?query=workflow%3ABuild)
[![License](https://img.shields.io/github/license/quarkiverse/quarkus-unleash.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Central](https://img.shields.io/maven-central/v/io.quarkiverse.unleash/quarkus-unleash-parent?color=green)](https://search.maven.org/search?q=g:io.quarkiverse.unleash%20AND%20a:quarkus-unleash-parent)
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-2-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

## Usage

To use the extension, add the dependency to the target project:

```xml
<dependency>
  <groupId>io.quarkiverse.unleash</groupId>
  <artifactId>quarkus-unleash</artifactId>
  <version>{latest-maven-release}</version>
</dependency>
```

## Simple usage

Assuming you have Unleash running on localhost:4242 you should add the following properties to your application.properties and fill in the values for url.

```properties
quarkus.unleash.url=http://localhost:4242/api
```

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

## Configuration

| Property    | Description | Type        | Default |
| ----------- | ----------- | ----------- | ----------- |
| quarkus.unleash.url  | Unleash URL service endpoint       | string | [required] |
| quarkus.unleash.application  | Application name       | string |  |
| quarkus.unleash.project  | Project name       | string |  |
| quarkus.unleash.instance-id  | Instance ID       | string |  |
| quarkus.unleash.disable-metrics  | Disable Unleash metrics       | boolean | false |
| quarkus.unleash.token  | Application Unleash token       | string |  |
| quarkus.unleash.environment  | Application environment       | string |  |
| quarkus.unleash.fetch-toggles-interval  | Fetch toggles interval (in seconds)  | long| 10 |
| quarkus.unleash.send-metrics-interval  | Send metrics interval (in seconds)  | long| 60 |
| quarkus.unleash.backup-file  | Backup File       | string |  |
| quarkus.unleash.synchronous-fetch-on-initialisation  | A synchronous fetch on initialisation | boolean | false |
| quarkus.unleash.enable-proxy-authentication-by-jvm-properties  | Enable proxy authentication by JVM properties | boolean | false |

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
