import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
<#if language == "kotlin">
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
</#if>

plugins {
<#if language == "kotlin">
<#if vertxVersion?starts_with("5.")>
  kotlin ("jvm") version "2.2.20"
<#else>
  kotlin ("jvm") version "2.0.0"
</#if>
<#elseif language == "scala">
  scala
<#else>
  java
</#if>
  application
  id("com.gradleup.shadow") version "9.2.2"
}

group = "${groupId}"
version = "1.0.0-SNAPSHOT"

repositories {
<#if vertxVersion?ends_with("-SNAPSHOT")>
  maven {
    url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    mavenContent {
      snapshotsOnly()
    }
  }
</#if>
  mavenCentral()
}

val vertxVersion = "${vertxVersion}"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "${packageName}.MainVerticle"
<#if vertxVersion?starts_with("5.")>
val launcherClassName = "io.vertx.launcher.application.VertxApplication"
<#else>
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
<#noparse>
val doOnChange = "${projectDir}/gradlew classes"
</#noparse>
</#if>

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
<#if !vertxDependencies?has_content>
  implementation("io.vertx:vertx-core")
</#if>
<#if vertxVersion?starts_with("5.")>
  implementation("io.vertx:vertx-launcher-application")
</#if>
<#list vertxDependencies as dependency>
  implementation("io.vertx:${dependency}")
</#list>
<#if language == "kotlin" && vertxVersion?starts_with("4.")>
  implementation(kotlin("stdlib-jdk8"))
<#elseif language == "scala">
  implementation("org.scala-lang:scala3-library_3:3.5.2")
  implementation("io.vertx:vertx-lang-scala_3:${vertxVersion}")
</#if>
<#if hasPgClient && vertxVersion?starts_with("4.")>
  implementation("com.ongres.scram:client:2.1")
</#if>
<#if language == "scala">
  testImplementation("io.vertx:vertx-lang-scala-test_3:${vertxVersion}")
  testImplementation("org.scalatest:scalatest_3:3.2.19")
  testRuntimeOnly("org.scalatestplus:junit-5-11_3:3.2.19.0")
<#elseif hasVertxJUnit5>
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
<#elseif hasVertxUnit>
  testImplementation("io.vertx:vertx-unit")
  testImplementation("junit:junit:4.13.2")
</#if>
}

<#if language == "kotlin">
kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.fromTarget("${jdkVersion?switch('17' '17', '21' '21', '25' '25', '17')}")
<#if vertxVersion?starts_with("5.")>
    languageVersion = KotlinVersion.fromVersion("2.0")
    apiVersion = KotlinVersion.fromVersion("2.0")
  <#else>
    languageVersion = KotlinVersion.fromVersion("1.7")
    apiVersion = KotlinVersion.fromVersion("1.7")
  </#if>
  }
}
<#else>
java {
  sourceCompatibility = JavaVersion.VERSION_${jdkVersion?replace(".", "_")}
  targetCompatibility = JavaVersion.VERSION_${jdkVersion?replace(".", "_")}
}
</#if>

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
<#if language == "scala">
  useJUnitPlatform()
<#elseif hasVertxJUnit5>
  useJUnitPlatform()
<#elseif hasVertxUnit>
  useJUnit()
</#if>
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
<#if vertxVersion?starts_with("5.")>
  args = listOf(mainVerticleName)
<#else>
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
</#if>
}
