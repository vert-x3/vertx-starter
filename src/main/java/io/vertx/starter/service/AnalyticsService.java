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

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.starter.model.VertxProject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.UUID;

public class AnalyticsService {

  private static final Logger log = LogManager.getLogger(AnalyticsService.class);

  private final Vertx vertx;
  private final Path analyticsDir;

  public AnalyticsService(Vertx vertx, Path analyticsDir) {
    this.vertx = vertx;
    this.analyticsDir = analyticsDir;
  }

  public void onProjectCreated(Message<VertxProject> message) {
    log.debug("Building analytics with on new project created");
    VertxProject project = message.body();
    var document = toDocument(project);
    var path = analyticsDir.resolve(UUID.randomUUID().toString()).toAbsolutePath();
    try {
      vertx.fileSystem().writeFile(path.toString(), document.toBuffer()).await();
    } catch (Exception e) {
      log.error("Failed to write file {}", path, e);
    }
  }

  private JsonObject toDocument(VertxProject project) {
    var document = JsonObject.mapFrom(project);
    document.remove("id");
    document.remove("groupId");
    document.remove("artifactId");
    document.remove("packageName");
    return document;
  }
}
