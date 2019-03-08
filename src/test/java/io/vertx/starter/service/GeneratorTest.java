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

import com.julienviet.childprocess.Process;
import com.julienviet.childprocess.ProcessOptions;
import io.netty.buffer.ByteBufInputStream;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.core.impl.Utils;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.starter.GeneratorVerticle;
import io.vertx.starter.config.Topics;
import io.vertx.starter.model.BuildTool;
import io.vertx.starter.model.Language;
import io.vertx.starter.model.VertxProject;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.vertx.starter.model.ArchiveFormat.TGZ;
import static io.vertx.starter.model.BuildTool.GRADLE;
import static io.vertx.starter.model.BuildTool.MAVEN;
import static io.vertx.starter.model.Language.JAVA;
import static io.vertx.starter.model.Language.KOTLIN;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thomas Segismont
 */
@ExtendWith(VertxExtension.class)
@Timeout(value = 1, timeUnit = TimeUnit.MINUTES)
class GeneratorTest {

  private MessageProducer<JsonObject> producer;
  private Path workdir;

  @BeforeEach
  void beforeEach(Vertx vertx, VertxTestContext testContext) throws IOException {
    producer = vertx.eventBus().publisher(Topics.PROJECT_REQUESTED);
    workdir = Files.createTempDirectory(GeneratorTest.class.getSimpleName());
    vertx.deployVerticle(new GeneratorVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  private VertxProject defaultProject() {
    return new VertxProject()
      .setId("demo")
      .setGroupId("com.example")
      .setArtifactId("demo")
      .setLanguage(JAVA)
      .setBuildTool(MAVEN)
      .setVertxVersion("3.6.3")
      .setVertxDependencies(Collections.singleton("vertx-web"))
      .setArchiveFormat(TGZ);
  }

  @Test
  void java_with_maven_tgz(Vertx vertx, VertxTestContext testContext) {
    testProject(vertx, testContext, defaultProject());
  }

  @Test
  void java_with_gradle_tgz(Vertx vertx, VertxTestContext testContext) {
    testProject(vertx, testContext, defaultProject().setBuildTool(GRADLE));
  }

  @Test
  void kotlin_with_maven_tgz(Vertx vertx, VertxTestContext testContext) {
    testProject(vertx, testContext, defaultProject().setLanguage(KOTLIN));
  }

  @Test
  void kotlin_with_gradle_tgz(Vertx vertx, VertxTestContext testContext) {
    testProject(vertx, testContext, defaultProject().setLanguage(KOTLIN).setBuildTool(GRADLE));
  }

  private void testProject(Vertx vertx, VertxTestContext testContext, VertxProject project) {
    producer.<Buffer>send(JsonObject.mapFrom(project), testContext.succeeding(msg -> {
      unpack(vertx, testContext, workdir, msg.body(), testContext.succeeding(unpacked -> {
        testContext.verify(() -> {

          verifyBaseFiles();

          BuildTool buildTool = project.getBuildTool();
          Language language = project.getLanguage();
          if (buildTool == MAVEN) {
            verifyMavenFiles();
          } else if (buildTool == GRADLE) {
            verifyGradleFiles(language);
          } else {
            testContext.failNow(new NoStackTraceThrowable(unsupported(buildTool)));
            return;
          }

          verifySourceFiles(language);

          if (Utils.isWindows()) {
            // For now, we won't test on Windows, it's tested on Travis anyway
            testContext.completeNow();
          } else {

            buildProject(vertx, buildTool, testContext.succeeding(projectBuilt -> {
              testContext.verify(() -> {

                if (buildTool == MAVEN) {
                  verifyMavenOutputFiles();
                } else if (buildTool == GRADLE) {
                  verifyGradleOutputFiles();
                } else {
                  testContext.failNow(new NoStackTraceThrowable(unsupported(buildTool)));
                }

                testContext.completeNow();
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
    assertThat(workdir.resolve(".mvn/wrapper/MavenWrapperDownloader.java")).isRegularFile();
  }

  private void verifyMavenOutputFiles() {
    assertThat(workdir.resolve("target/surefire-reports/com.example.demo.TestMainVerticle.txt")).isRegularFile();
    assertThat(workdir.resolve("target/demo-1.0.0-SNAPSHOT-fat.jar")).isRegularFile();
  }

  private void verifyGradleFiles(Language language) {
    assertThat(workdir.resolve("build.gradle")).isRegularFile();
    assertThat(workdir.resolve("settings.gradle")).isRegularFile();
    if (language == KOTLIN) {
      assertThat(workdir.resolve("gradle.properties")).isRegularFile();
    }
    assertThat(workdir.resolve("gradlew")).isRegularFile().isExecutable();
    assertThat(workdir.resolve("gradlew.bat")).isRegularFile();
    assertThat(workdir.resolve("gradle/wrapper/gradle-wrapper.properties")).isRegularFile();
    assertThat(workdir.resolve("gradle/wrapper/gradle-wrapper.jar")).isRegularFile();
  }

  private void verifyGradleOutputFiles() {
    assertThat(workdir.resolve("build/test-results/test/TEST-com.example.demo.TestMainVerticle.xml")).isRegularFile();
    assertThat(workdir.resolve("build/libs/demo-1.0.0-SNAPSHOT-fat.jar")).isRegularFile();
  }

  private void verifySourceFiles(Language language) {
    String verticleFile = String.format("src/main/%s/com/example/demo/MainVerticle%s", language.getName(), language.getExtension());
    assertThat(workdir.resolve(verticleFile)).isRegularFile();
    String testFile = String.format("src/test/%s/com/example/demo/TestMainVerticle%s", language.getName(), language.getExtension());
    assertThat(workdir.resolve(testFile)).isRegularFile();
  }

  private void buildProject(Vertx vertx, BuildTool buildTool, Handler<AsyncResult<Void>> handler) {
    ProcessOptions processOptions = new ProcessOptions().setCwd(workdir.toString());
    String command;
    List<String> args;
    if (buildTool == MAVEN) {
      command = "./mvnw";
      args = Collections.singletonList("verify");
    } else if (buildTool == GRADLE) {
      command = "./gradlew";
      args = Arrays.asList("assemble", "check");
    } else {
      handler.handle(Future.failedFuture(unsupported(buildTool)));
      return;
    }
    Process process = Process.create(vertx, command, args, processOptions);
    Buffer buffer = Buffer.buffer();
    process.stdout().handler(buffer::appendBuffer);
    process.stderr().handler(buffer::appendBuffer);
    process.exitHandler(code -> {
      if (code == 0) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(String.format("Failed to build project%n%s", buffer.toString())));
      }
    });
    process.start();
  }

  private void unpack(Vertx vertx, VertxTestContext testContext, Path workdir, Buffer buffer, Handler<AsyncResult<Void>> handler) {
    vertx.executeBlocking(fut -> {
      try (ByteBufInputStream bbis = new ByteBufInputStream(buffer.getByteBuf());
           TarArchiveInputStream archiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(bbis))) {

        TarArchiveEntry entry = null;
        while ((entry = archiveInputStream.getNextTarEntry()) != null) {

          if (!archiveInputStream.canReadEntryData(entry)) {
            fut.fail("Can't read entry " + entry.getName());
            return;
          }
          File f = workdir.resolve(entry.getName()).toFile();
          if (entry.isDirectory()) {
            if (!f.isDirectory() && !f.mkdirs()) {
              fut.fail(new IOException("Failed to create directory " + f));
              return;
            }
          } else {
            File parent = f.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
              fut.fail(new IOException("Failed to create directory " + parent));
              return;
            }
            try (OutputStream outputStream = Files.newOutputStream(f.toPath())) {
              IOUtils.copy(archiveInputStream, outputStream);
              if (entry.getMode() == 0100744) {
                f.setExecutable(true);
              }
            }
          }
        }

        fut.complete();

      } catch (IOException e) {
        fut.fail(e);
      }
    }, false, handler);
  }

  private String unsupported(BuildTool buildTool) {
    return "Unsupported build tool: " + buildTool.name();
  }
}
