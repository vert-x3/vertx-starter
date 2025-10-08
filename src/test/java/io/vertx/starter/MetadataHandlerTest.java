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

package io.vertx.starter;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.starter.service.MetadataHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Daniel Petisme
 * @author Thomas Segismont
 */
@ExtendWith(VertxExtension.class)
public class MetadataHandlerTest {

  private HttpServer server;
  private JsonObject defaults;
  private JsonArray versions;
  private JsonArray stack;
  private HttpClient httpClient;

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) throws IOException {
    JsonObject starterData = Util.loadStarterData();

    defaults = starterData.getJsonObject("defaults");
    versions = starterData.getJsonArray("versions");
    stack = starterData.getJsonArray("stack");

    Router router = Router.router(vertx);
    router.route().handler(new MetadataHandler(defaults, versions, stack));

    server = vertx.createHttpServer(new HttpServerOptions().setPort(0));
    server
      .requestHandler(router)
      .listen()
      .onComplete(testContext.succeeding(srv -> {
        httpClient = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(srv.actualPort()));
        testContext.completeNow();
      }));
  }

  @Test
  public void shouldReturnStarterMetadata(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.wrap(httpClient);
    webClient.get("/").send().onComplete(testContext.succeeding(response -> testContext.verify(() -> {

      assertThat(response.statusCode()).withFailMessage(response.bodyAsString()).isEqualTo(200);

      JsonObject metadata = response.bodyAsJsonObject();
      assertThat(metadata.getJsonObject("defaults")).isEqualTo(defaults);
      assertThat(metadata.getJsonArray("versions")).isEqualTo(versions);
      assertThat(metadata.getJsonArray("stack")).isEqualTo(stack);
      assertThat(metadata.getJsonArray("buildTools")).contains("maven", "gradle");
      assertThat(metadata.getJsonArray("languages")).contains("java", "kotlin");
      assertThat(metadata.getJsonArray("jdkVersions")).contains("17", "21", "25");
      assertThat(metadata.getJsonArray("vertxDependencies")).isEqualTo(stack);
      assertThat(metadata.getJsonArray("vertxVersions")).isEqualTo(versions.stream()
        .map(JsonObject.class::cast)
        .map(obj -> obj.getString("number"))
        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll));

      testContext.completeNow();

    })));
  }

  @Test
  public void shouldHandleEtag(Vertx vertx, VertxTestContext testContext) {
    WebClient webClient = WebClient.wrap(httpClient);
    webClient.get("/").send().onComplete(testContext.succeeding(response -> testContext.verify(() -> {

      assertThat(response.statusCode()).withFailMessage(response.bodyAsString()).isEqualTo(200);
      assertThat(response.headers().contains(HttpHeaders.CACHE_CONTROL)).isTrue();
      assertThat(response.headers().contains(HttpHeaders.ETAG)).isTrue();

      String etag = response.headers().get(HttpHeaders.ETAG);

      webClient.get("/").putHeader(HttpHeaders.IF_NONE_MATCH.toString(), etag).send().onComplete(testContext.succeeding(cachedResp -> testContext.verify(() -> {
        assertThat(cachedResp.statusCode()).isEqualTo(304);
        assertThat(cachedResp.body()).isNull();
        testContext.completeNow();
      })));
    })));
  }

  @AfterEach
  void tearDown(Vertx vertx, VertxTestContext testContext) {
    Future<Void> clientClose = httpClient != null ? httpClient.close() : Future.succeededFuture();
    Future<Void> serverClose = server != null ? server.close() : Future.succeededFuture();
    Future.join(clientClose, serverClose)
      .eventually(() -> vertx.close())
      .onComplete(testContext.succeedingThenComplete());
  }
}
