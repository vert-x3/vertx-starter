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
import io.vertx.ext.mongo.MongoClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.starter.AnalyticsVerticle;
import io.vertx.starter.config.Topics;
import io.vertx.starter.model.VertxProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.Collections;

import static io.vertx.starter.model.ArchiveFormat.TGZ;
import static io.vertx.starter.model.BuildTool.MAVEN;
import static io.vertx.starter.model.Language.JAVA;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thomas Segismont
 */
@ExtendWith(VertxExtension.class)
@Testcontainers
class AnalyticsTest {

  @Container
  GenericContainer mongo = new GenericContainer<>("library/mongo:3.4").withExposedPorts(27017);

  private MongoClient client;

  @BeforeEach
  void beforeEach(Vertx vertx, VertxTestContext testContext) throws IOException {
    JsonObject config = new JsonObject()
      .put("host", mongo.getContainerIpAddress())
      .put("port", mongo.getMappedPort(27017));

    client = MongoClient.createNonShared(vertx, config);

    DeploymentOptions options = new DeploymentOptions().setConfig(config);
    vertx.deployVerticle(new AnalyticsVerticle(), options, testContext.succeeding(id -> testContext.completeNow()));
  }

  @AfterEach
  void afterEach(Vertx vertx, VertxTestContext testContext) {
    client.close();
    testContext.completeNow();
  }

  static VertxProject defaultProject() {
    return new VertxProject()
      .setId("demo")
      .setGroupId("analytics")
      .setArtifactId("test")
      .setLanguage(JAVA)
      .setBuildTool(MAVEN)
      .setVertxVersion("3.6.3")
      .setVertxDependencies(Collections.singleton("vertx-web"))
      .setArchiveFormat(TGZ);
  }

  @Test
  void projectPersisted(Vertx vertx, VertxTestContext testContext) {
    vertx.eventBus().publish(Topics.PROJECT_CREATED, JsonObject.mapFrom(defaultProject()));
    vertx.setTimer(500, l -> {
      JsonObject query = new JsonObject();
      client.find(AnalyticsService.COLLECTION_NAME, query, testContext.succeeding(list -> {
        testContext.verify(() -> {
          assertThat(list).hasSize(1);
          JsonObject jsonObject = list.get(0);
          assertThat(jsonObject.getString("groupId")).isEqualTo("analytics");
          assertThat(jsonObject.getString("artifactId")).isEqualTo("test");
          testContext.completeNow();
        });
      }));
    });
  }
}
