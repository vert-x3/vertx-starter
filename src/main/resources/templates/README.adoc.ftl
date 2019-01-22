= ${artifactId?capitalize}

image:https://img.shields.io/badge/vert.x-${vertxVersion}-purple.svg[link="https://vertx.io"]

This application was generated using http://start.vertx.io

== Building

To launch your tests:
<#if buildTool == "maven">
```
./mvnw clean test
```
</#if>
<#if buildTool == 'gradle'>
```
./gradlew clean test
```
</#if>

To package your application:
<#if buildTool == "maven">
```
./mvnw clean package
```
</#if>
<#if buildTool == 'gradle'>
```
./gradlew clean assemble
```
</#if>

To run your application:
<#if buildTool == "maven">
```
./mvnw clean exec:java
```
</#if>
<#if buildTool == 'gradle'>
```
./gradlew clean run
```
</#if>

== Help

* https://vertx.io/docs/[Vert.x Documentation]
* https://stackoverflow.com/questions/tagged/vert.x?sort=newest&pageSize=15[Vert.x Stack Overflow]
* https://groups.google.com/forum/?fromgroups#!forum/vertx[Vert.x User Group]
* https://gitter.im/eclipse-vertx/vertx-users[Vert.x Gitter]


