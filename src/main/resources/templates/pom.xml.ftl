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
    <kotlin.version>1.3.20</kotlin.version>
    <kotlin.compiler.incremental>true</kotlin.compiler.incremental>

<#else>
<#if jdkVersion == "1.8">
    <maven.compiler.source>${jdkVersion}</maven.compiler.source>
    <maven.compiler.target>${jdkVersion}</maven.compiler.target>

</#if>
    <maven-compiler-plugin.version>3.8.0</maven-compiler-plugin.version>
</#if>
    <maven-shade-plugin.version>2.4.3</maven-shade-plugin.version>
    <maven-surefire-plugin.version>2.22.1</maven-surefire-plugin.version>
    <exec-maven-plugin.version>1.5.0</exec-maven-plugin.version>

    <vertx.version>${vertxVersion}</vertx.version>
    <junit-jupiter.version>5.4.0</junit-jupiter.version>

    <main.verticle>${packageName}.MainVerticle</main.verticle>
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
<#list vertxDependencies as dependency>
    <dependency>
      <groupId>io.vertx</groupId>
      <artifactId>${dependency}</artifactId>
    </dependency>
</#list>
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
          <jvmTarget>1.8</jvmTarget>
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
<#if jdkVersion == "11">
        <configuration>
          <release>${jdkVersion}</release>
        </configuration>
</#if>
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
                    <Main-Class>io.vertx.core.Launcher</Main-Class>
<#noparse>
                    <Main-Verticle>${main.verticle}</Main-Verticle>
</#noparse>
                  </manifestEntries>
                </transformer>
                <transformer
                  implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
                  <resource>META-INF/services/io.vertx.core.spi.VerticleFactory</resource>
                </transformer>
              </transformers>
              <artifactSet>
              </artifactSet>
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
          <mainClass>io.vertx.core.Launcher</mainClass>
          <arguments>
            <argument>run</argument>
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
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
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
