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
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;

public class StarterMetadataService {

  private static final Logger log = LoggerFactory.getLogger(StarterMetadataService.class);

  private static final String DEPENDENCIES_PATH = "dependencies.json";
  public static final String VERSION_PROVIDER_API_URL = "https://api.bintray.com/packages/bintray/jcenter/io.vertx:vertx-core";
  public static final JsonArray DEFAULT_VERSIONS = new JsonArray()
    .add("3.6.2");

  private final WebClient webClient;
  private JsonArray dependencies;

  public StarterMetadataService(Vertx vertx) {
    loadDependencies(vertx.fileSystem());
    this.webClient = WebClient.create(vertx);
  }

  private void loadDependencies(FileSystem fileSystem) {
    log.debug("Loading dependencies from {}", DEPENDENCIES_PATH);
    fileSystem.readFile(DEPENDENCIES_PATH, ar -> {
      if (ar.succeeded()) {
        String raw = ar.result().toString();
        this.dependencies = new JsonObject(raw).getJsonArray("content");
        log.info("Vert.x dependencies loaded");
      } else {
        log.error("Impossible to load dependencies {}: {}", DEPENDENCIES_PATH, ar.cause().getMessage());
      }
    });
  }

  public void getAllVertxVersions(Handler<AsyncResult<JsonArray>> reply) {
    //TODO cache result
    log.debug("Finding Vert.x version via Bintray");
    webClient.getAbs(VERSION_PROVIDER_API_URL).send(ar -> {
      if (ar.succeeded()) {
        JsonObject data = new JsonObject(ar.result().body());
        reply.handle(Future.succeededFuture(data.getJsonArray("versions")));
      } else {
        log.error("Providing default versions because: {}", ar.cause());
        reply.handle(Future.succeededFuture(DEFAULT_VERSIONS));
      }
    });
  }

  public void getAllVertxDependencies(Handler<AsyncResult<JsonArray>> reply) {
    //TODO cache result
    log.debug("Finding Vert.x dependencies");
    reply.handle(Future.succeededFuture(this.dependencies));
  }

}
