# Vertx.x starter

**Disclaimer**: This is a *WIP* project. Any comment, test or help is welcome!

Vert.x starter 

## Installation

```
    git clone https://github.com/danielpetisme/vertx-starter.git
    cd vertx-starter
    ./build_and_run.sh
```

## Default values
Have a look to `vertx-starter-main/conf/default-conf.json`
```
{
    "api": {
        "http.port": 9090,
        "prefix": "api",
        "project.request": {
            "version": "3.5.0",
            "format": "zip",
            "language": "java",
            "build": "maven",
            "groupId": "io.vertx",
            "artifactId": "sample",
            "dependencies": [
                "vertx-core",
                "vertx-unit"
            ]
        },
        "dependencies.path": "./dependencies.json"
    },
    "web": {
        "http.port": 8080
    },
    "generator": {
        "temp.dir": "."
    }
}
```
## Generating a project

Simply click on "Generate Project" on the web interface to download a project archive.

It your a CLI adept, you can achieve the same result with `curl`

`$ http://<api_url>/starter.zip -o starter.zip`

All the web ui inputs are mapped to the following attributes

* Basic information for the generated project `groupId`, `artifactId`
* `version`: the Vert.x version
* `build`: `maven` or `gradle` build tool
* `language`: `java`, `groovy`, `kotlin`, `js`, `scala`
* `dependencies`: a comma separated list of artifactIds  of the vert.x modules

Full example:
```
$ http://<api_url>/starter.zip \
-d version=3.5.0 \
-d language=java \
-d groupId=io.vertx \ 
-d artifactId=sample \
-d depencies=vertx-web,vertx-web-client \
-o starter.zip`
```
