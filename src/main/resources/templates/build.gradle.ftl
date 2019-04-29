plugins {
<#if language == "kotlin">
  id 'org.jetbrains.kotlin.jvm' version '1.3.20'
<#else>
  id 'java'
</#if>
  id 'application'
  id 'com.github.johnrengelman.shadow' version '5.0.0'
}

group = '${groupId}'
version = '1.0.0-SNAPSHOT'

repositories {
<#if vertxVersion?ends_with("-SNAPSHOT")>
  maven {
    url 'https://oss.sonatype.org/content/repositories/snapshots'
    mavenContent {
      snapshotsOnly()
    }
  }
</#if>
  mavenCentral()
<#if language == "kotlin">
  jcenter()
</#if>
}

ext {
<#if language == "kotlin">
  kotlinVersion = '1.3.20'
</#if>
  vertxVersion = '${vertxVersion}'
  junitJupiterEngineVersion = '5.4.0'
}

application {
  mainClassName = 'io.vertx.core.Launcher'
}

<#if language != "kotlin">
sourceCompatibility = '${jdkVersion}'
</#if>

def mainVerticleName = '${packageName}.MainVerticle'
def watchForChange = 'src/**/*'
def doOnChange = './gradlew classes'

dependencies {
  implementation "io.vertx:vertx-core:$vertxVersion"
<#list vertxDependencies as dependency>
  implementation "io.vertx:${dependency}:$vertxVersion"
</#list>

  testImplementation "io.vertx:vertx-junit5:$vertxVersion"
  testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine:$junitJupiterEngineVersion"
  testImplementation "org.junit.jupiter:junit-jupiter-api:$junitJupiterEngineVersion"
}

<#if language == "kotlin">
compileKotlin {
  kotlinOptions.jvmTarget = '1.8'
}

compileTestKotlin {
  kotlinOptions.jvmTarget = '1.8'
}

</#if>

shadowJar {
  classifier = 'fat'
  manifest {
    attributes 'Main-Verticle': mainVerticleName
  }
  mergeServiceFiles {
    include 'META-INF/services/io.vertx.core.spi.VerticleFactory'
  }
}

test {
  useJUnitPlatform()
  testLogging {
    events 'PASSED', 'FAILED', 'SKIPPED'
  }
}

run {
  args = ['run', mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$mainClassName", "--on-redeploy=$doOnChange"]
}
