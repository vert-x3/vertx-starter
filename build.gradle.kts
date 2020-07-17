/*
 * Copyright (c) 2017-2018 Daniel Petisme
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType.ALL

plugins {
  id("io.vertx.vertx-plugin") version "1.1.0"
}

repositories {
  jcenter()
  mavenLocal()
}

group = "io.vertx"
version = "2.0.9"
description = "A web application to generate Vert.x projects"

val junitJupiterVersion = "5.6.2"
val testContainersVersion = "1.14.3"

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation("io.vertx:vertx-web")
  implementation("io.vertx:vertx-mongo-client")
  implementation("io.vertx:vertx-web-client")
  implementation("io.vertx:vertx-web-templ-freemarker")

  implementation("org.apache.commons:commons-compress:1.18")
  implementation("ch.qos.logback:logback-classic:1.2.3")
  implementation("io.github.jponge:vertx-boot:1.0.0")

  testImplementation("org.assertj:assertj-core:3.10.0")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.testcontainers:testcontainers:${testContainersVersion}")
  testImplementation("com.julienviet:childprocess-vertx-ext:1.3.0")

  testImplementation("org.junit.jupiter:junit-jupiter:${junitJupiterVersion}")
  testImplementation("org.testcontainers:junit-jupiter:${testContainersVersion}")
}

vertx {
  vertxVersion = "3.9.2"
  mainVerticle = "io.github.jponge.vertx.boot.BootVerticle"
  jvmArgs = listOf("-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory")
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
  failFast = true
  jvmArgs("-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory")
  environment("TESTCONTAINERS_RYUK_DISABLED", true)
}

tasks.withType<ShadowJar> {
  archiveFileName.set("${project.name}-${project.version}-fat.jar")
}


tasks.withType<Wrapper> {
  gradleVersion = "5.2.1"
  distributionType = ALL
}

apply(from = "gradle/docker.gradle")
