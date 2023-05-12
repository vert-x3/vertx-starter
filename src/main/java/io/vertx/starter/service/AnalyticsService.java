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

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.starter.model.VertxProject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnalyticsService {

  private static final Logger log = LogManager.getLogger(AnalyticsService.class);

  static final String COLLECTION_NAME = "projects";

  private final MongoClient mongoClient;

  public AnalyticsService(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  public void onProjectCreated(Message<VertxProject> message) {
    log.debug("Building analytics with on new project created");
    VertxProject project = message.body();
    JsonObject document = toDocument(project);
    mongoClient.save(COLLECTION_NAME, document, res -> {
      if (res.failed()) {
        log.error("Failed to save document", res.cause());
      }
    });
  }

  private JsonObject toDocument(VertxProject project) {
    JsonObject document = JsonObject.mapFrom(project);
    document.remove("id");
    document.remove("groupId");
    document.remove("artifactId");
    document.remove("packageName");
    String createdOn = document.getString("createdOn");
    document.put("createdOn", new JsonObject().put("$date", createdOn));
    return document;
  }
}
