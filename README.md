# Vertx.x Starter

[![Build Status](https://github.com/vert-x3/vertx-starter/workflows/CI/badge.svg)](https://github.com/vert-x3/vertx-starter/actions?query=workflow%3ACI)

[Vert.x Starter](http://start.vertx.io) is an open-source web application for creating [Vert.x](https://vertx.io/) applications.

## Quickstart

Simply click on _Generate Project_ on the web interface to download a project archive.

If you are a CLI adept, you can use any http client (curl, [httpie](https://httpie.org/)) to invoke the API.

```
$ curl -X GET http://start.vertx.io/starter.zip -d groupId=com.acme -d language=java -d vertxVersion=4.1.2 -o starter.zip
```

## API

## Generating a Vert.x application

```
http://start.vertx.io/starter.{archiveFormat}
```

*Note*: `{archiveFormat}` can be `zip`, `tgz`, `tar.gz`, etc. if the project generator can handle the format, it will use the appropriate compression tool.

You can provide the following query parameters to customize the project

- Basic information for the generated project `groupId`, `artifactId`
- `language`: `java` or `kotlin`
- `buildTool`: `maven` or `gradle` build tool
- `vertxVersion`: the Vert.x version
- `vertxDependencies`: a comma separated list of artifactIds of the vert.x modules
- `packageName`: code package name, derived from `groupId` and `artifactId` by default
- `jdkVersion`: which version of the JDK to use, defaults to `1.8`

Full example:

```
curl -X GET \
  'http://start.vertx.io/starter.zip?artifactId=starter&buildTool=maven&groupId=io.vertx&language=java&vertxDependencies=&vertxVersion=4.1.2' \
  -o starter.zip
```

The HTTPie equivalent:

```
$ http http://start.vertx.io/starter.zip \
groupId==io.vertx \
artitfactId==starter \
language==java \
buildTool==maven \
vertxVersion==4.1.2 \
vertxDependencies==vertx-web,vertx-web-client \
-o starter.zip
```

## Vert.x Starter metadata

The vert.x starter metadata lists all the capabilities proposed by the API. The metadata is used to build the Web UI is exposed to ease the creation of third-party clients (IDE integration, CLI, etc).

```
http://start.vertx.io/metadata
```

## Running your own starter

### Build from sources

For now, the vertx-starter project is not available on Maven-Central, so you need to build it from source.

In order to build it, you will need Java 1.8.

### Building fat jar

```
$ ./gradlew shadowJar
```

### Running the app locally

```
$ ./gradlew vertxRun
```

Note: you need MongoDB.
You may run it using Docker:

```
$ docker run --rm -d -p 27017:27017 mongo
```

### Configuration

Vert.x starter relies on the [`vertx-boot`](https://github.com/jponge/vertx-boot) launcher.
The application is configured by [`src/main/resources/application.conf`](./src/main/resources/application.conf).
Please see the according documentation to know how to override the configuration.

## Releasing

To release the project, proceed as follows.

First, tag the last commit in `master` branch:

```
export VERSION=x.y.z
git tag -f -a ${VERSION} -m "Version ${VERSION}"
git push upstream  --tags
```

Close the corresponding [milestone](https://github.com/vert-x3/vertx-starter/milestones) on GitHub.

Then merge the `master` branch into the `prod` branch:

```
git checkout prod
git merge master
git push
```

GitHub _Deploy_ job will automatically redeploy the starter if the `prod` branch build passes.

Eventually, checkout `master` again and update the version property in the Gradle build file.
For example:

```
git checkout master
sed -i -e 's/version = "2\.0\.4"/version = "2.0.5"/' build.gradle.kts
git commit -a -m "Set version to 2.0.5"
git push
```

## License

Vert.x Starter is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
