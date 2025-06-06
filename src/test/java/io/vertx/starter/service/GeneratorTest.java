/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.starter.service;

import io.reactiverse.childprocess.Process;
import io.reactiverse.childprocess.ProcessOptions;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.Utils;
import io.vertx.core.json.JsonObject;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.starter.GeneratorVerticle;
import io.vertx.starter.Util;
import io.vertx.starter.VertxProjectCodec;
import io.vertx.starter.config.Topics;
import io.vertx.starter.model.BuildTool;
import io.vertx.starter.model.Language;
import io.vertx.starter.model.VertxProject;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.vertx.starter.model.ArchiveFormat.TGZ;
import static io.vertx.starter.model.BuildTool.GRADLE;
import static io.vertx.starter.model.BuildTool.MAVEN;
import static io.vertx.starter.model.JdkVersion.*;
import static io.vertx.starter.model.Language.KOTLIN;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

/**
 * @author Thomas Segismont
 */
@ExtendWith(VertxExtension.class)
@Timeout(value = 5, timeUnit = TimeUnit.MINUTES)
@Tag("generator")
class GeneratorTest {

  static Path tempDir;
  static String java11Home;
  static String java17Home;
  static String java21Home;
  static Path m2dir;
  static Path mavenRepository;
  static Path settingsFile;

  Path workdir;
  List<Runnable> cleanupTasks;

  @BeforeAll
  static void beforeAll() throws Exception {
    tempDir = Files.createTempDirectory(GeneratorTest.class.getName());
    java11Home = computeJavaHome("JAVA_HOME_11_X64", 11);
    java17Home = computeJavaHome("JAVA_HOME_17_X64", 17);
    java21Home = computeJavaHome("JAVA_HOME_21_X64", 21);
    m2dir = tempDir.resolve("m2");
    Files.createDirectories(m2dir);
    mavenRepository = tempDir.resolve("repository");
    Files.createDirectories(mavenRepository);
    settingsFile = m2dir.resolve("settings.xml");
    ClassLoader classLoader = GeneratorTest.class.getClassLoader();
    Files.copy(Objects.requireNonNull(classLoader.getResourceAsStream("settings-test.xml")), settingsFile);
  }

  private static String computeJavaHome(String generatorJavaHomeEnv, int minimumJavaVersion) {
    String javaHome = System.getenv().getOrDefault(generatorJavaHomeEnv, System.getProperty("java.home"));
    Path versionFile = tempDir.resolve("version");
    File bin = new File(javaHome, "bin");
    File java = new File(bin, "java");
    int javaVersion;
    try {
      java.lang.Process process = new ProcessBuilder(java.getAbsolutePath(), "--version")
        .directory(bin)
        .redirectOutput(versionFile.toFile())
        .start();
      process.waitFor(5, SECONDS);
      javaVersion = Integer.parseInt(Files.readAllLines(versionFile)
        .get(0)
        .split("\\s")[1]
        .split("\\.")[0]);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (Exception ignored) {
      return null;
    }
    return javaVersion >= minimumJavaVersion ? javaHome : null;
  }

  @BeforeEach
  void beforeEach(Vertx vertx, VertxTestContext testContext) throws Exception {
    workdir = tempDir.resolve(UUID.randomUUID().toString());
    Files.createDirectories(workdir);
    cleanupTasks = new ArrayList<>();
    vertx.eventBus().registerDefaultCodec(VertxProject.class, new VertxProjectCodec());
    vertx.deployVerticle(new GeneratorVerticle()).onComplete(testContext.succeeding(id -> testContext.completeNow()));
  }

  @AfterEach
  void afterEach() {
    cleanupTasks.forEach(Runnable::run);
  }

  @AfterAll
  static void afterAll() throws Exception {
    try (Stream<Path> pathStream = Files.walk(tempDir)) {
      pathStream
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
    }
  }

  static VertxProject defaultProject() {
    return new VertxProject()
      .setId("demo")
      .setGroupId("com.example")
      .setArtifactId("demo")
      .setArchiveFormat(TGZ);
  }

  static Stream<VertxProject> testProjects() throws IOException {
    List<String> versions = Util.loadStarterData().getJsonArray("versions").stream()
      .map(JsonObject.class::cast)
      .map(obj -> obj.getString("number"))
      .filter(version -> !version.endsWith("-SNAPSHOT"))
      .toList();

    List<Set<String>> testDeps = Arrays.asList(Collections.singleton("vertx-unit"), Collections.singleton("vertx-junit5"));

    Stream.Builder<VertxProject> builder = Stream.builder();
    for (BuildTool buildTool : BuildTool.values()) {
      for (Language language : Language.values()) {
        for (String version : versions) {
          for (Set<String> vertxDependencies : testDeps) {
            VertxProject vertxProject = defaultProject()
              .setBuildTool(buildTool)
              .setLanguage(language)
              .setVertxVersion(version)
              .setVertxDependencies(new HashSet<>(vertxDependencies))
              .setPackageName("com.mycompany.project.special");
            builder.add(vertxProject);
          }
        }
      }
    }
    return builder.build();
  }

  @ParameterizedTest
  @MethodSource("testProjectsJdk11")
  @Tag("generator-11")
  void testProjectJdk11(VertxProject project, Vertx vertx, VertxTestContext testContext) {
    assumeThat(java11Home).isNotNull();
    testProject(project, vertx, java11Home, testContext);
  }

  static Stream<VertxProject> testProjectsJdk11() throws IOException {
    return testProjects().map(vertxProject -> vertxProject.setJdkVersion(JDK_11));
  }

  @ParameterizedTest
  @MethodSource("testProjectsJdk17")
  @Tag("generator-17")
  void testProjectJdk17(VertxProject project, Vertx vertx, VertxTestContext testContext) {
    assumeThat(java17Home).isNotNull();
    testProject(project, vertx, java17Home, testContext);
  }

  static Stream<VertxProject> testProjectsJdk17() throws IOException {
    return testProjects().map(vertxProject -> vertxProject.setJdkVersion(JDK_17));
  }

  @ParameterizedTest
  @MethodSource("testProjectsJdk21")
  @Tag("generator-21")
  void testProjectJdk21(VertxProject project, Vertx vertx, VertxTestContext testContext) {
    assumeThat(java21Home).isNotNull();
    testProject(project, vertx, java21Home, testContext);
  }

  static Stream<VertxProject> testProjectsJdk21() throws IOException {
    return testProjects().map(vertxProject -> vertxProject.setJdkVersion(JDK_21));
  }

  private void testProject(VertxProject project, Vertx vertx, String javaHome, VertxTestContext testContext) {
    vertx.eventBus().<Buffer>request(Topics.PROJECT_REQUESTED, project).onComplete(testContext.succeeding(msg -> {
      unpack(vertx, workdir, msg.body(), testContext.succeeding(unpacked -> {
        testContext.verify(() -> {

          verifyBaseFiles();

          BuildTool buildTool = project.getBuildTool();
          Language language = project.getLanguage();
          if (buildTool == MAVEN) {
            verifyMavenFiles();
          } else if (buildTool == GRADLE) {
            verifyGradleFiles(language);
          } else {
            testContext.failNow(unsupported(buildTool));
            return;
          }

          try {
            verifySourceFiles(language);
          } catch (IOException e) {
            throw new AssertionError(e);
          }

          if (Utils.isWindows()) {
            testContext.completeNow();
          } else {

            buildProject(vertx, buildTool, javaHome, testContext.succeeding(projectBuilt -> {
              testContext.verify(() -> {

                switch (buildTool) {
                  case MAVEN -> {
                    try {
                      verifyMavenOutputFiles();
                    } catch (IOException e) {
                      throw new AssertionError(e);
                    }
                  }
                  case GRADLE -> {
                    try {
                      verifyGradleOutputFiles();
                    } catch (IOException e) {
                      throw new AssertionError(e);
                    }
                  }
                  default -> testContext.failNow(unsupported(buildTool));
                }

                runDevMode(vertx, buildTool, javaHome, testContext.succeeding(devModeRan -> testContext.completeNow()));
              });
            }));
          }
        });
      }));
    }));
  }

  private void verifyBaseFiles() {
    assertThat(workdir.resolve(".editorconfig")).isRegularFile();
    assertThat(workdir.resolve(".gitignore")).isRegularFile();
    assertThat(workdir.resolve("README.adoc")).isRegularFile();
  }

  private void verifyMavenFiles() {
    assertThat(workdir.resolve("pom.xml")).isRegularFile();
    assertThat(workdir.resolve("mvnw")).isRegularFile().isExecutable();
    assertThat(workdir.resolve("mvnw.cmd")).isRegularFile();
    assertThat(workdir.resolve(".mvn/wrapper/maven-wrapper.properties")).isRegularFile();
    assertThat(workdir.resolve(".mvn/wrapper/maven-wrapper.jar")).isRegularFile().isExecutable();
  }

  private void verifyMavenOutputFiles() throws IOException {
    try (Stream<Path> pathStream = Files.walk(workdir)) {
      Optional<Path> testResult = pathStream.filter(p -> p.toString().endsWith("TestMainVerticle.txt")).findFirst();
      assertThat(testResult).isPresent().hasValueSatisfying(p -> assertThat(p).isRegularFile());
      assertThat(workdir.resolve("target/demo-1.0.0-SNAPSHOT-fat.jar")).isRegularFile();
    }
  }

  private void verifyGradleFiles(Language language) {
    assertThat(workdir.resolve("build.gradle.kts")).isRegularFile();
    assertThat(workdir.resolve("settings.gradle.kts")).isRegularFile();
    if (language == KOTLIN) {
      assertThat(workdir.resolve("gradle.properties")).isRegularFile();
    }
    assertThat(workdir.resolve("gradlew")).isRegularFile().isExecutable();
    assertThat(workdir.resolve("gradlew.bat")).isRegularFile();
    assertThat(workdir.resolve("gradle/wrapper/gradle-wrapper.properties")).isRegularFile();
    assertThat(workdir.resolve("gradle/wrapper/gradle-wrapper.jar")).isRegularFile();
  }

  private void verifyGradleOutputFiles() throws IOException {
    try (Stream<Path> pathStream = Files.walk(workdir)) {
      Optional<Path> testResult = pathStream.filter(p -> p.toString().endsWith("TestMainVerticle.xml")).findFirst();
      assertThat(testResult).isPresent().hasValueSatisfying(p -> assertThat(p).isRegularFile());
      assertThat(workdir.resolve("build/libs/demo-1.0.0-SNAPSHOT-fat.jar")).isRegularFile();
    }
  }

  private void verifySourceFiles(Language language) throws IOException {
    try (Stream<Path> pathStream = Files.walk(workdir.resolve("src/main/" + language.getName()))) {
      Optional<Path> verticleFile = pathStream
        .filter(p -> p.endsWith("MainVerticle" + language.getExtension()))
        .findFirst();
      assertThat(verticleFile).isPresent().hasValueSatisfying(p -> assertThat(p).isRegularFile());
    }
    try (Stream<Path> pathStream = Files.walk(workdir.resolve("src/test/" + language.getName()))) {
      Optional<Path> testFile = pathStream
        .filter(p -> p.endsWith("TestMainVerticle" + language.getExtension()))
        .findFirst();
      assertThat(testFile).isPresent().hasValueSatisfying(p -> assertThat(p).isRegularFile());
    }
  }

  private void buildProject(Vertx vertx, BuildTool buildTool, String javaHome, Handler<AsyncResult<Void>> handler) {
    ProcessOptions processOptions = new ProcessOptions().setCwd(workdir.toString());
    processOptions.getEnv().put("JAVA_HOME", javaHome);

    String command;
    List<String> args;
    if (buildTool == MAVEN) {
      command = "./mvnw";
      args = Stream.<String>builder()
        .add("-Dmaven.repo.local=" + mavenRepository.toAbsolutePath())
        .add("-s")
        .add(settingsFile.toAbsolutePath().toString())
        .add("-B")
        .add("verify")
        .build()
        .collect(toList());
    } else if (buildTool == GRADLE) {
      command = "./gradlew";
      args = Stream.<String>builder()
        .add("--no-build-cache")
        .add("--no-daemon")
        .add("assemble")
        .add("check")
        .build()
        .collect(toList());
    } else {
      handler.handle(Future.failedFuture(unsupported(buildTool)));
      return;
    }
    Process.create(vertx, command, args, processOptions).startHandler(process -> {
      cleanupTasks.add(() -> process.kill(true));
      Buffer buffer = Buffer.buffer();
      process.stdout().handler(buffer::appendBuffer);
      process.stderr().handler(buffer::appendBuffer);
      process.exitHandler(code -> {
        if (code == 0) {
          handler.handle(Future.succeededFuture());
        } else {
          handler.handle(Future.failedFuture(String.format("Failed to build project%n%s", buffer)));
        }
      });
    }).start();
  }

  private void runDevMode(Vertx vertx, BuildTool buildTool, String javaHome, Handler<AsyncResult<Void>> handler) {
    ProcessOptions processOptions = new ProcessOptions().setCwd(workdir.toString());
    processOptions.getEnv().put("JAVA_HOME", javaHome);

    String command;
    List<String> args;
    if (buildTool == MAVEN) {
      command = "./mvnw";
      args = Arrays.asList("clean", "compile", "exec:java");
    } else if (buildTool == GRADLE) {
      command = "./gradlew";
      args = Arrays.asList("clean", "run");
    } else {
      handler.handle(Future.failedFuture(unsupported(buildTool)));
      return;
    }
    Process.create(vertx, command, args, processOptions).startHandler(process -> {
      cleanupTasks.add(() -> process.kill(true));
      Promise<Void> promise = Promise.promise();
      promise.future().onComplete(handler);
      RecordParser parser = RecordParser.newDelimited("\n")
        .exceptionHandler(promise::tryFail)
        .handler(buffer -> {
          String line = buffer.toString().trim();
          if (line.contains("HTTP server started on port 8888")) {
            promise.tryComplete();
          }
        });
      process.stdout().exceptionHandler(promise::tryFail).handler(parser);
    }).start();
  }

  @SuppressWarnings("OctalInteger")
  private void unpack(Vertx vertx, Path workdir, Buffer buffer, Handler<AsyncResult<Void>> handler) {
    vertx.<Void>executeBlocking(() -> {
      try (ByteArrayInputStream bais = new ByteArrayInputStream(buffer.getBytes());
           TarArchiveInputStream ais = new TarArchiveInputStream(new GzipCompressorInputStream(bais))) {

        TarArchiveEntry entry;
        while ((entry = ais.getNextTarEntry()) != null) {

          if (!ais.canReadEntryData(entry)) {
            throw new IOException("Can't read entry " + entry.getName());
          }
          File f = workdir.resolve(entry.getName()).toFile();
          if (entry.isDirectory()) {
            if (!f.isDirectory() && !f.mkdirs()) {
              throw new IOException("Failed to create directory " + f);
            }
          } else {
            File parent = f.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
              throw new IOException("Failed to create directory " + parent);
            }
            try (OutputStream outputStream = Files.newOutputStream(f.toPath())) {
              IOUtils.copy(ais, outputStream);
              if (entry.getMode() == 0100744) {
                f.setExecutable(true);
              }
            }
          }
        }

        return null;
      }
    }, false).onComplete(handler);
  }

  private String unsupported(BuildTool buildTool) {
    return "Unsupported build tool: " + buildTool.name();
  }
}
