import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
<#if language == "kotlin">
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
</#if>

plugins {
<#if language == "kotlin">
  kotlin ("jvm") version "1.5.10"
<#elseif language == "scala">
  scala
<#else>
  java
</#if>
  application
  id("com.github.johnrengelman.shadow") version "7.0.0"
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
  mavenLocal()
  mavenCentral()
}

val vertxVersion = "${vertxVersion}"
val junitJupiterVersion = "5.7.0"
<#if language == "scala">
val mainVerticleName = "scala:${packageName}.MainVerticle"
<#else>
val mainVerticleName = "${packageName}.MainVerticle"
</#if>
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
<#noparse>
val doOnChange = "${projectDir}/gradlew classes"
</#noparse>

application {
  mainClass.set(launcherClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
<#if language == "scala">
  val scalaVersion = "2.13"
  implementation("org.scala-lang:scala-library:2.13.6")
  testImplementation("io.vertx:vertx-lang-scala-test_$scalaVersion:$vertxVersion")

  testImplementation("org.scalatest:scalatest_$scalaVersion:3.2.6")
  testImplementation("co.helmethair:scalatest-junit-runner:0.1.9")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
</#if>
<#if !vertxDependencies?has_content>
  implementation("io.vertx:vertx-core")
</#if>
<#list vertxDependencies as dependency>
  implementation("io.vertx:${dependency}")
</#list>
<#if language == "kotlin">
  implementation(kotlin("stdlib-jdk8"))
</#if>
<#if hasVertxJUnit5 && language != "scala">
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
<#elseif hasVertxUnit  && language != "scala">
  testImplementation("io.vertx:vertx-unit")
  testImplementation("junit:junit:4.13.1")
</#if>
}

<#if language == "kotlin">
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "${jdkVersion}"
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
<#if hasVertxJUnit5>
  useJUnitPlatform()
<#elseif hasVertxUnit>
  useJUnit()
</#if>
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
