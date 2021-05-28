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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.time.Instant;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Thomas Segismont
 */
@ExtendWith(VertxExtension.class)
@Testcontainers
class AnalyticsTest {

  @Container
  MongoDBContainer mongo = new MongoDBContainer("mongo:3.4");

  private MongoClient client;

  @BeforeEach
  void beforeEach(Vertx vertx, VertxTestContext testContext) throws IOException {
    vertx.eventBus().registerDefaultCodec(VertxProject.class, new VertxProjectCodec());

    JsonObject config = new JsonObject()
      .put("host", mongo.getContainerIpAddress())
      .put("port", mongo.getMappedPort(27017));

    client = MongoClient.create(vertx, config);

    DeploymentOptions options = new DeploymentOptions().setConfig(config);
    vertx.deployVerticle(new AnalyticsVerticle(), options, testContext.succeeding(id -> testContext.completeNow()));
  }

  @AfterEach
  void afterEach(Vertx vertx, VertxTestContext testContext) {
    client.close();
    testContext.completeNow();
  }

  @Test
  void projectPersisted(Vertx vertx, VertxTestContext testContext) {
    Instant createdOn = Instant.now().truncatedTo(MINUTES);
    VertxProject vertxProject = new VertxProject()
      .setId("should-not-persist")
      .setGroupId("should-not-persist")
      .setArtifactId("should-not-persist")
      .setPackageName("should-not-persist")
      .setCreatedOn(createdOn)
      .setOperatingSystem("Other");
    vertx.eventBus().publish(Topics.PROJECT_CREATED, vertxProject);
    Checkpoint checkpoint = testContext.laxCheckpoint();
    vertx.setPeriodic(20, id -> {
      JsonObject query = new JsonObject();
      client.find(AnalyticsService.COLLECTION_NAME, query, testContext.succeeding(list -> {
        testContext.verify(() -> {
          assertTrue(list.size() <= 1);
          if (list.size() == 1) {
            JsonObject document = list.get(0);
            assertFalse(Stream.of("id", "groupId", "artifactId", "packageName").anyMatch(document::containsKey));
            assertEquals(createdOn, document.getJsonObject("createdOn").getInstant("$date"));
            assertEquals(vertxProject.getOperatingSystem(), document.getString("operatingSystem"));
            checkpoint.flag();
            assertTrue(vertx.cancelTimer(id));
          }
        });
      }));
    });
  }
}
