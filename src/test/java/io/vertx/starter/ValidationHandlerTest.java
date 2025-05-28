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

package io.vertx.starter;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.starter.model.VertxProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.Collections;

import static io.vertx.starter.config.ProjectConstants.*;
import static io.vertx.starter.model.ArchiveFormat.ZIP;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thomas Segismont
 */
@ExtendWith(VertxExtension.class)
class ValidationHandlerTest {

  private JsonObject defaults;
  private JsonArray versions;
  private JsonArray stack;
  private ValidationHandler validator;
  private MultiMap params;

  @BeforeEach
  void setUp() throws IOException {
    JsonObject starterData = Util.loadStarterData();
    defaults = starterData.getJsonObject("defaults");
    versions = starterData.getJsonArray("versions");
    stack = starterData.getJsonArray("stack");
    validator = new ValidationHandler(defaults, versions, stack);
    params = MultiMap.caseInsensitiveMultiMap();
  }

  @Test
  void testDefaults(Vertx vertx, VertxTestContext testContext) {
    expectSuccess(vertx, testContext, validator, params, ZIP.getFileExtension(), defaults.mapTo(VertxProject.class));
  }

  @Test
  void testInvalidGroupId(Vertx vertx, VertxTestContext testContext) {
    expectFailure(vertx, testContext, validator, params.add(GROUP_ID, "é;;"), ZIP.getFileExtension());
  }

  @Test
  void testInvalidArtifactId(Vertx vertx, VertxTestContext testContext) {
    expectFailure(vertx, testContext, validator, params.add(ARTIFACT_ID, "é;;"), ZIP.getFileExtension());
  }

  @Test
  void testUnknownLanguage(Vertx vertx, VertxTestContext testContext) {
    expectFailure(vertx, testContext, validator, params.add(LANGUAGE, "rust"), ZIP.getFileExtension());
  }

  @Test
  void testUnknownBuildTool(Vertx vertx, VertxTestContext testContext) {
    expectFailure(vertx, testContext, validator, params.add(BUILD_TOOL, "ivy"), ZIP.getFileExtension());
  }

  @Test
  void testUnknownVersion(Vertx vertx, VertxTestContext testContext) {
    expectFailure(vertx, testContext, validator, params.add(VERTX_VERSION, "1.0.2"), ZIP.getFileExtension());
  }

  @Test
  void testUnknownDependency(Vertx vertx, VertxTestContext testContext) {
    expectFailure(vertx, testContext, validator, params.add(VERTX_DEPENDENCIES, "vertx-coffee-machine"), ZIP.getFileExtension());
  }

  @Test
  void testDependencyIncluded(Vertx vertx, VertxTestContext testContext) {
    validator = new ValidationHandler(defaults, versions, stack);
    VertxProject expected = defaults.mapTo(VertxProject.class).setVertxDependencies(Collections.singleton("vertx-web"));
    expectSuccess(vertx, testContext, validator, params.add(VERTX_DEPENDENCIES, "vertx-web"), ZIP.getFileExtension(), expected);
  }

  @Test
  void testDependencyExcluded(Vertx vertx, VertxTestContext testContext) {
    defaults.put("vertxVersion", "3.6.3");
    versions = new JsonArray()
      .add(new JsonObject().put("number", "3.6.3").put("exclusions", new JsonArray().add("vertx-web-graphql")));
    stack = new JsonArray()
      .add(new JsonObject()
        .put("category", "Web")
        .put("items", new JsonArray().add(new JsonObject().put("artifactId", "vertx-web-graphql")))
      );
    validator = new ValidationHandler(defaults, versions, stack);
    expectFailure(vertx, testContext, validator, params.add(VERTX_DEPENDENCIES, "vertx-web-graphql"), ZIP.getFileExtension());
  }

  @Test
  void testUnknownArchiveFormat(Vertx vertx, VertxTestContext testContext) {
    expectFailure(vertx, testContext, validator, params, ".rar");
  }

  @Test
  void testInvalidPackageName(Vertx vertx, VertxTestContext testContext) {
    expectFailure(vertx, testContext, validator, params.add(PACKAGE_NAME, "é;;"), ZIP.getFileExtension());
  }

  @Test
  void testUnknownJdkVersion(Vertx vertx, VertxTestContext testContext) {
    expectFailure(vertx, testContext, validator, params.add(JDK_VERSION, "9"), ZIP.getFileExtension());
  }

  @Test
  void testTwoJunitVersions(Vertx vertx, VertxTestContext testContext) {
    MultiMap params = this.params.add(VERTX_DEPENDENCIES, "vertx-unit,vertx-junit5");
    expectFailure(vertx, testContext, validator, params, ZIP.getFileExtension());
  }

  private void expectSuccess(Vertx vertx, VertxTestContext testContext, ValidationHandler validator, MultiMap params, String extension, VertxProject expected) {
    doTest(vertx, testContext, validator, params, extension, response -> {
      testContext.verify(() -> {
        assertThat(response.statusCode()).withFailMessage(response.bodyAsString()).isEqualTo(200);
        VertxProject actual = Json.decodeValue(response.body(), VertxProject.class);
        assertThat(actual).usingRecursiveComparison()
          .ignoringFields("id", "operatingSystem", "createdOn")
          .isEqualTo(expected);
        testContext.completeNow();
      });
    });
  }

  private void expectFailure(Vertx vertx, VertxTestContext testContext, ValidationHandler validator, MultiMap params, String extension) {
    doTest(vertx, testContext, validator, params, extension, response -> {
      testContext.verify(() -> {
        assertThat(response.statusCode()).withFailMessage(response.bodyAsString()).isEqualTo(400);
        String message = new JsonObject(response.body()).getString("message");
        assertThat(message).isNotEmpty();
        testContext.completeNow();
      });
    });
  }

  private void doTest(Vertx vertx, VertxTestContext testContext, ValidationHandler validator, MultiMap params, String extension, Handler<HttpResponse<Buffer>> handler) {
    Router router = Router.router(vertx);
    router.route().handler(validator).handler(rc -> {
      VertxProject vertProject = rc.get(WebVerticle.VERTX_PROJECT_KEY);
      rc.response().end(Json.encodePrettily(vertProject));
    });

    vertx.createHttpServer(new HttpServerOptions().setPort(0))
      .requestHandler(router)
      .listen()
      .onComplete(testContext.succeeding(server -> {

        WebClientOptions options = new WebClientOptions().setDefaultPort(server.actualPort());
        WebClient webClient = WebClient.create(vertx, options);

        HttpRequest<Buffer> request = webClient.get("/starter" + extension);
        request.queryParams().addAll(params);

        request.send().onComplete(testContext.succeeding(handler));
      }));
  }
}
