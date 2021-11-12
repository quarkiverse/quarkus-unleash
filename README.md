# Quarkus Unleash

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![Build](https://github.com/quarkiverse/quarkus-unleash/workflows/Build/badge.svg?branch=main)](https://github.com/quarkiverse/quarkus-unleash/actions?query=workflow%3ABuild)
[![License](https://img.shields.io/github/license/quarkiverse/quarkus-unleash.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Central](https://img.shields.io/maven-central/v/io.quarkiverse.unleash/quarkus-unleash-parent?color=green)](https://search.maven.org/search?q=g:io.quarkiverse.unleash%20AND%20a:quarkus-unleash-parent)
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
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

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://www.lorislab.org"><img src="https://avatars2.githubusercontent.com/u/828045?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Andrej Petras</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkiverse-unleash/commits?author=andrejpetras" title="Code">💻</a> <a href="#maintenance-andrejpetras" title="Maintenance">🚧</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification.
Contributions of any kind welcome!
