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
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.starter.AnalyticsVerticle;
import io.vertx.starter.VertxProjectCodec;
import io.vertx.starter.config.Topics;
import io.vertx.starter.model.VertxProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static io.vertx.starter.config.VerticleConfigurationConstants.Analytics.ANALYTICS_DIR_CONF;
import static io.vertx.starter.model.ArchiveFormat.ZIP;
import static io.vertx.starter.model.BuildTool.GRADLE;
import static io.vertx.starter.model.JdkVersion.JDK_17;
import static io.vertx.starter.model.Language.KOTLIN;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Segismont
 */
@ExtendWith(VertxExtension.class)
class AnalyticsTest {

  @TempDir
  Path analyticsDir;

  @BeforeEach
  void beforeEach(Vertx vertx, VertxTestContext testContext) throws IOException {
    vertx.eventBus().registerDefaultCodec(VertxProject.class, new VertxProjectCodec());

    JsonObject config = new JsonObject().put(ANALYTICS_DIR_CONF, analyticsDir.toString());
    DeploymentOptions options = new DeploymentOptions().setConfig(config);
    vertx.deployVerticle(new AnalyticsVerticle(), options, testContext.succeeding(id -> testContext.completeNow()));
  }

  @AfterEach
  void afterEach(VertxTestContext testContext) {
    testContext.completeNow();
  }

  @Test
  void projectPersisted(Vertx vertx, VertxTestContext testContext) {
    VertxProject vertxProject = new VertxProject()
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
    Checkpoint checkpoint = testContext.laxCheckpoint();
    vertx.setPeriodic(20, id -> {
      vertx.fileSystem().readDir(analyticsDir.toString()).onComplete(testContext.succeeding(ls -> {
        if (ls.isEmpty()) {
          return;
        }
        testContext.verify(() -> assertEquals(1, ls.size()));
        String file = analyticsDir.resolve(ls.get(0)).toString();
        vertx.fileSystem().readFile(file).onComplete(testContext.succeeding(buffer -> {
          JsonObject document = new JsonObject(buffer);
          testContext.verify(() -> {
            assertFalse(Stream.of("id", "groupId", "artifactId", "packageName").anyMatch(document::containsKey));
            assertEquals(vertxProject.getLanguage().getName(), document.getString("language"));
            assertEquals(vertxProject.getBuildTool().getValue(), document.getString("buildTool"));
            assertEquals(vertxProject.getVertxVersion(), document.getString("vertxVersion"));
            assertEquals(vertxProject.getVertxDependencies(), Set.copyOf(document.getJsonArray("vertxDependencies").getList()));
            assertEquals(vertxProject.getArchiveFormat().toString().toLowerCase(Locale.ROOT), document.getString("archiveFormat"));
            assertEquals(vertxProject.getJdkVersion().getValue(), document.getString("jdkVersion"));
            assertEquals(vertxProject.getOperatingSystem(), document.getString("operatingSystem"));
            assertEquals(vertxProject.getCreatedOn(), document.getInstant("createdOn"));
            assertTrue(vertx.cancelTimer(id));
            checkpoint.flag();
          });
        }));
      }));
    });
  }
}
