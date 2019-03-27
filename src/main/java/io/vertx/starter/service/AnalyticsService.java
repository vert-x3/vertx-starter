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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsService {

  private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

  static final String COLLECTION_NAME = "projects";

  private final MongoClient mongoClient;

  public AnalyticsService(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  public void onProjectCreated(Message<JsonObject> message) {
    log.debug("Building analytics with on new project created");
    JsonObject document = message.body();
    mongoClient.save(COLLECTION_NAME, document, res -> {
      if (res.failed()) {
        log.error("Failed to save project {}: {}", document, res.cause().getMessage());
      } else {
        log.debug("Saved project: {}", document);
      }
    });
  }
}
