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

package io.vertx.starter.web.rest;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.starter.WebVerticle;
import io.vertx.starter.config.Topics;
import io.vertx.starter.config.VerticleConfigurationConstants;
import io.vertx.starter.model.ArchiveFormat;
import io.vertx.starter.model.BuildTool;
import io.vertx.starter.model.Language;
import io.vertx.starter.model.VertxProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashSet;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class StarterResourceTest {

  private static final int HTTP_PORT = 9000;
  private WebClient webClient;
  private JsonObject config;

  private static VertxProject defaultProject() {
    VertxProject defaultProject = new VertxProject();
    defaultProject.setGroupId("AA.BB.CC");
    defaultProject.setArtifactId("DD");
    defaultProject.setLanguage(Language.JAVA);
    defaultProject.setBuildTool(BuildTool.MAVEN);
    defaultProject.setVertxVersion("0.0.0");
    defaultProject.setVertxDependencies(new HashSet<>(asList("EE", "FF")));
    defaultProject.setArchiveFormat(ArchiveFormat.ZIP);
    return defaultProject;
  }

  private static JsonObject testConfig() {
    JsonObject config = new JsonObject();
    config.put(VerticleConfigurationConstants.Web.HTTP_PORT, HTTP_PORT);
    config.put(VerticleConfigurationConstants.Web.PROJECT_DEFAULTS, JsonObject.mapFrom(defaultProject()));
    return config;
  }

  @BeforeEach
  public void beforeEach(Vertx vertx, VertxTestContext testContext) {
    this.config = testConfig();
    vertx.deployVerticle(
      new WebVerticle(),
      new DeploymentOptions().setConfig(config),
      testContext.succeeding(id -> {
        vertx.eventBus().<VertxProject>consumer(Topics.PROJECT_REQUESTED).unregister();
        this.webClient = WebClient.create(vertx, new WebClientOptions().setDefaultPort(HTTP_PORT));
        testContext.completeNow();
      })
    );
  }

  @Test
  @DisplayName("should create project with default values when details not provided")
  public void shouldCreateProjectWithDefaultValuesWhenDetailsNotProvided(Vertx vertx, VertxTestContext testContext) {
    vertx.eventBus().<VertxProject>consumer(Topics.PROJECT_REQUESTED).handler(message -> {
      VertxProject project = message.body();
      testContext.verify(() -> {
        assertThat(project).isEqualToIgnoringGivenFields(defaultProject(), "id", "operatingSystem", "createdOn");
        vertx.fileSystem().readFile("web/starter.zip", testContext.succeeding(buffer -> {
          message.reply(buffer);
        }));
      });
    });
    webClient.get("/starter.dummy")
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(HTTP_OK);
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("should return HTTP 500 when the generated archive is invalid")
  public void shouldReturnFailureWhenGeneratedArchiveIsInvalid(Vertx vertx, VertxTestContext testContext) {
    vertx.eventBus().<VertxProject>consumer(Topics.PROJECT_REQUESTED).handler(message -> {
      VertxProject project = message.body();
      testContext.verify(() -> {
        assertThat(project).isEqualToIgnoringGivenFields(defaultProject(), "id", "operatingSystem", "createdOn");
        message.fail(-1, "Failure");
      });
    });
    webClient.get("/starter.zip")
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(HTTP_INTERNAL_ERROR);
        testContext.completeNow();
      })));
  }

  @Test
  @DisplayName("should return Starter metadata")
  public void shouldReturnStarterMetadata(Vertx vertx, VertxTestContext testContext) {
    webClient.get("/metadata")
      .send(testContext.succeeding(response -> testContext.verify(() -> {
        assertThat(response.statusCode()).isEqualTo(HTTP_OK);
        JsonObject metadata = response.bodyAsJsonObject();
        assertThat(metadata.getJsonArray("languages")).contains("java", "kotlin");
        assertThat(metadata.getJsonArray("buildTools")).contains("maven", "gradle");
        assertThat(metadata.getJsonObject("defaults")).isEqualTo(this.config.getJsonObject("project-defaults"));
        testContext.completeNow();
      })));
  }

}
