<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>${groupId}</groupId>
  <artifactId>${artifactId}</artifactId>
  <version>1.0.0-SNAPSHOT</version>

  <properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

<#if language == "kotlin">
<#if vertxVersion?starts_with("5.")>
    <kotlin.version>2.0.0</kotlin.version>
<#else>
    <kotlin.version>1.7.21</kotlin.version>
</#if>

<#else>
    <maven-compiler-plugin.version>3.8.1</maven-compiler-plugin.version>
</#if>
    <maven-shade-plugin.version>3.2.4</maven-shade-plugin.version>
    <maven-surefire-plugin.version>2.22.2</maven-surefire-plugin.version>
    <exec-maven-plugin.version>3.0.0</exec-maven-plugin.version>

    <vertx.version>${vertxVersion}</vertx.version>
<#if hasVertxJUnit5>
    <junit-jupiter.version>5.9.1</junit-jupiter.version>
</#if>

    <main.verticle>${packageName}.MainVerticle</main.verticle>
<#if vertxVersion?starts_with("5.")>
    <launcher.class>io.vertx.launcher.application.VertxApplication</launcher.class>
<#else>
    <launcher.class>io.vertx.core.Launcher</launcher.class>
</#if>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>io.vertx</groupId>
        <artifactId>vertx-stack-depchain</artifactId>
<#noparse>
        <version>${vertx.version}</version>
</#noparse>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
<#if !vertxDependencies?has_content>
    <dependency>
      <groupId>io.vertx</groupId>
      <artifactId>vertx-core</artifactId>
    </dependency>
</#if>
<#if vertxVersion?starts_with("5.")>
    <dependency>
      <groupId>io.vertx</groupId>
      <artifactId>vertx-launcher-application</artifactId>
    </dependency>
</#if>
<#list vertxDependencies as dependency>
    <dependency>
      <groupId>io.vertx</groupId>
      <artifactId>${dependency}</artifactId>
    </dependency>
</#list>
<#if language == "kotlin">
<#if vertxVersion?starts_with("5.")>
<#noparse>
    <dependency>
      <groupId>org.jetbrains.kotlin</groupId>
      <artifactId>kotlin-stdlib</artifactId>
      <version>${kotlin.version}</version>
    </dependency>
</#noparse>
<#else>
<#noparse>
    <dependency>
      <groupId>org.jetbrains.kotlin</groupId>
      <artifactId>kotlin-stdlib-jdk8</artifactId>
      <version>${kotlin.version}</version>
    </dependency>
</#noparse>
</#if>
</#if>
<#if hasPgClient>
<#if vertxVersion?starts_with("5.")>
  <dependency>
    <groupId>com.ongres.scram</groupId>
    <artifactId>scram-client</artifactId>
    <version>3.1</version>
  </dependency>
<#else>
  <dependency>
  <groupId>com.ongres.scram</groupId>
  <artifactId>client</artifactId>
  <version>2.1</version>
  </dependency>
</#if>
</#if>

<#if hasVertxJUnit5>
    <dependency>
      <groupId>io.vertx</groupId>
      <artifactId>vertx-junit5</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-api</artifactId>
<#noparse>
      <version>${junit-jupiter.version}</version>
</#noparse>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter-engine</artifactId>
<#noparse>
      <version>${junit-jupiter.version}</version>
</#noparse>
      <scope>test</scope>
    </dependency>
<#elseif hasVertxUnit>
    <dependency>
      <groupId>io.vertx</groupId>
      <artifactId>vertx-unit</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.13.2</version>
      <scope>test</scope>
    </dependency>
</#if>
  </dependencies>

  <build>
<#if language == "kotlin">
<#noparse>
      <sourceDirectory>${project.basedir}/src/main/kotlin</sourceDirectory>
      <testSourceDirectory>${project.basedir}/src/test/kotlin</testSourceDirectory>
</#noparse>
</#if>
    <plugins>
<#if language == "kotlin">
      <plugin>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-maven-plugin</artifactId>
<#noparse>
        <version>${kotlin.version}</version>
</#noparse>
        <configuration>
<#if vertxVersion?starts_with("5.")>
          <jvmTarget>${jdkVersion?switch('11', '11', '17' '17', '21' '21', '17')}</jvmTarget>
<#else>
          <jvmTarget>${jdkVersion?switch('11', '11', '17' '17', '17')}</jvmTarget>
</#if>
        </configuration>
        <executions>
          <execution>
            <id>compile</id>
            <goals>
              <goal>compile</goal>
            </goals>
          </execution>
          <execution>
            <id>test-compile</id>
            <goals>
              <goal>test-compile</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
<#else>
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
<#noparse>
        <version>${maven-compiler-plugin.version}</version>
</#noparse>
        <configuration>
          <release>${jdkVersion}</release>
        </configuration>
      </plugin>
</#if>
      <plugin>
        <artifactId>maven-shade-plugin</artifactId>
<#noparse>
        <version>${maven-shade-plugin.version}</version>
</#noparse>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
            <configuration>
              <transformers>
                <transformer
                  implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                  <manifestEntries>
<#noparse>
                    <Main-Class>${launcher.class}</Main-Class>
                    <Main-Verticle>${main.verticle}</Main-Verticle>
</#noparse>
                  </manifestEntries>
                </transformer>
                <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
              </transformers>
<#noparse>
              <outputFile>${project.build.directory}/${project.artifactId}-${project.version}-fat.jar
</#noparse>
              </outputFile>
            </configuration>
          </execution>
        </executions>
      </plugin>
      <plugin>
        <artifactId>maven-surefire-plugin</artifactId>
<#noparse>
        <version>${maven-surefire-plugin.version}</version>
</#noparse>
      </plugin>
      <plugin>
        <groupId>org.codehaus.mojo</groupId>
        <artifactId>exec-maven-plugin</artifactId>
<#noparse>
        <version>${exec-maven-plugin.version}</version>
</#noparse>
        <configuration>
<#noparse>
          <mainClass>${launcher.class}</mainClass>
</#noparse>
          <arguments>
<#if vertxVersion?starts_with("4.")>
            <argument>run</argument>
</#if>
<#noparse>
            <argument>${main.verticle}</argument>
</#noparse>
          </arguments>
        </configuration>
      </plugin>
    </plugins>
  </build>

<#if vertxVersion?ends_with("-SNAPSHOT")>
  <repositories>
    <repository>
      <id>sonatype-oss-snapshots</id>
      <name>Sonatype OSSRH Snapshots</name>
      <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
      <layout>default</layout>
      <releases>
        <enabled>false</enabled>
        <updatePolicy>never</updatePolicy>
      </releases>
      <snapshots>
        <enabled>true</enabled>
        <updatePolicy>never</updatePolicy>
      </snapshots>
    </repository>
  </repositories>
</#if>

</project>
