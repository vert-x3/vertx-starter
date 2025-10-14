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

import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.starter.AnalyticsVerticle;
import io.vertx.starter.VertxProjectCodec;
import io.vertx.starter.config.Topics;
import io.vertx.starter.model.VertxProject;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static io.vertx.starter.config.VerticleConfigurationConstants.Analytics.ANALYTICS_DIR_CONF;
import static io.vertx.starter.model.ArchiveFormat.ZIP;
import static io.vertx.starter.model.BuildTool.GRADLE;
import static io.vertx.starter.model.JdkVersion.JDK_17;
import static io.vertx.starter.model.Language.KOTLIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Thomas Segismont
 */
@ExtendWith(VertxExtension.class)
class AnalyticsTest {

  @TempDir
  Path analyticsDir;

  @BeforeEach
  void beforeEach(Vertx vertx) {
    vertx.eventBus().registerDefaultCodec(VertxProject.class, new VertxProjectCodec());

    var config = new JsonObject().put(ANALYTICS_DIR_CONF, analyticsDir.toString());
    var options = new DeploymentOptions().setConfig(config).setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
    vertx.deployVerticle(new AnalyticsVerticle(), options).await();
  }

  @Test
  void projectPersisted(Vertx vertx) throws Exception {
    var vertxProject = new VertxProject()
      .setId("should-not-persist")
      .setGroupId("should-not-persist")
      .setArtifactId("should-not-persist")
      .setLanguage(KOTLIN)
      .setBuildTool(GRADLE)
      .setVertxVersion("4.4.2")
      .setVertxDependencies(Set.of("vertx-web", "vertx-pg-client"))
      .setArchiveFormat(ZIP)
      .setPackageName("should-not-persist")
      .setJdkVersion(JDK_17)
      .setOperatingSystem("Other")
      .setCreatedOn(Instant.now());

    vertx.eventBus().publish(Topics.PROJECT_CREATED, vertxProject);

    Awaitility.with()
      .pollInterval(Duration.ofMillis(20))
      .ignoreExceptions()
      .until(() -> {
        try (var files = Files.list(analyticsDir)) {
          return files.findFirst().isPresent();
        }
      });

    try (var pathStream = Files.list(analyticsDir)) {
      List<Path> files = pathStream.toList();
      assertEquals(1, files.size());

      var document = new JsonObject(Files.readString(files.getFirst()));
      assertFalse(Stream.of("id", "groupId", "artifactId", "packageName").anyMatch(document::containsKey));
      assertEquals(vertxProject.getLanguage().getName(), document.getString("language"));
      assertEquals(vertxProject.getBuildTool().getValue(), document.getString("buildTool"));
      assertEquals(vertxProject.getVertxVersion(), document.getString("vertxVersion"));
      assertEquals(vertxProject.getVertxDependencies(), Set.copyOf(document.getJsonArray("vertxDependencies").getList()));
      assertEquals(vertxProject.getArchiveFormat().toString().toLowerCase(Locale.ROOT), document.getString("archiveFormat"));
      assertEquals(vertxProject.getJdkVersion().getValue(), document.getString("jdkVersion"));
      assertEquals(vertxProject.getOperatingSystem(), document.getString("operatingSystem"));
      assertEquals(vertxProject.getCreatedOn(), document.getInstant("createdOn"));
    }
  }
}
