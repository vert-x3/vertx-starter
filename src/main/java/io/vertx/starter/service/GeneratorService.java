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

package io.vertx.starter.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.starter.model.VertxProject;
import org.gradle.tooling.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GeneratorService {

  private final Logger log = LoggerFactory.getLogger(GeneratorService.class);

  private final Vertx vertx;
  private final String generatorDir;
  private final String generatorOutputDir;
  private final ProjectConnection connection;

  public GeneratorService(String generatorDir, String generatorOutputDir, Vertx vertx) {
    this.vertx = vertx;
    this.generatorDir = generatorDir;
    this.generatorOutputDir = generatorOutputDir;
    GradleConnector connector = GradleConnector.newConnector();
    connector.forProjectDirectory(new File(generatorDir));
    this.connection = connector.connect();
    log.info("Gradle connection with project directory: {}", generatorDir );
  }

  private Path projectBuildDir(VertxProject project) {
    return Paths.get(generatorOutputDir, project.getId()).toAbsolutePath();
  }

  public void onProjectRequested(Message<JsonObject> message) {
    VertxProject project = message.body().mapTo(VertxProject.class);
    log.debug("Generating project: {}", project);
    generateProject(project, ar -> {
      if (ar.succeeded()) {
        log.info("Generation done fo VertxProject: {}", project);
        message.reply(ar.result());
      } else {
        log.error("Failed to generate project: {}: {}", project, ar.cause().getMessage());
        message.fail(500, ar.cause().getMessage());
      }
    });
  }

  public void onProjectCreated(Message<JsonObject> message) {
    VertxProject project = message.body().mapTo(VertxProject.class);
    Path projectBuildDir = projectBuildDir(project);
    log.debug("Cleaning project: {}", project);
    vertx.fileSystem().deleteRecursive(projectBuildDir.toString(), true, ar -> {
      if (ar.succeeded()) {
        log.debug("Cleaning done for VertxProject build dir: {}", projectBuildDir);
      } else {
        log.error("Impossible to clean {}", projectBuildDir);
      }
    });
  }

  public void generateProject(VertxProject project, Handler<AsyncResult<String>> handler) {
    log.info("Generating Project: {}", project);
    this.vertx.executeBlocking(blockingBuildFuture -> {
      // Configure the build
      Path projectBuildDir = projectBuildDir(project);
      BuildLauncher launcher = connection.newBuild();
      List<String> args = Arrays.asList(
        "-Dorg.gradle.project.buildDir=" + projectBuildDir.toString(),
        "-Ptype=" + project.getType(),
        "-PgroupId=" + project.getGroupId(),
        "-PartifactId=" + project.getArtifactId(),
        "-Planguage=" + project.getLanguage(),
        "-PbuildTool=" + project.getBuildTool(),
        "-PvertxVersion=" + project.getVertxVersion(),
        "-PvertxDependencies=" + String.join(",", project.getVertxDependencies()),
        "-ParchiveFormat=" + project.getArchiveFormat().getFileExtension()
      );
      launcher.withArguments(args);
      launcher.setStandardOutput(System.out);
      launcher.setStandardError(System.err);
      // Run the build
      log.info("Running {}/gradlew {}", this.generatorDir, this.generatorDir, String.join(" ", args));
      launcher.run(new ResultHandler<Void>() {
        @Override
        public void onComplete(Void aVoid) {
          String archivePath = projectBuildDir.resolve(project.getArtifactId() + "." + project.getArchiveFormat().getFileExtension()).toAbsolutePath().toString();
          blockingBuildFuture.complete(archivePath);
        }

        @Override
        public void onFailure(GradleConnectionException e) {
          blockingBuildFuture.fail(e);
        }
      });
    }, (AsyncResult<String> res) -> {
      if (res.succeeded()) {
        handler.handle(Future.succeededFuture(res.result()));
      } else {
        log.error("Failed to generate VertxProject {}", res.cause().getMessage());
        handler.handle(Future.failedFuture(res.cause()));
      }
    });
  }
}
