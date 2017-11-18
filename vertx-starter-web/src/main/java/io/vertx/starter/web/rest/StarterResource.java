/*
 * Copyright (c) 2017 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.starter.web.rest;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.vertx.starter.web.util.RestUtil.error;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public class StarterResource {

  public static List<String> DEFAULT_DEPENDENCIES = asList("vertx-core", "vertx-unit");
  public static final JsonObject DEFAULT_PROJECT_REQUEST = new JsonObject()
    .put("format", "zip")
    .put("language", "java")
    .put("build", "maven")
    .put("groupId", "io.vertx")
    .put("artifactId", "sample")
    .put("dependencies", new JsonArray(DEFAULT_DEPENDENCIES));
  private final Logger log = LoggerFactory.getLogger(StarterResource.class);
  private EventBus eventBus;
  private JsonObject defaultProjectRequest;

  public StarterResource(EventBus eventBus, JsonObject defaultProjectRequest) {
    requireNonNull(eventBus);
    requireNonNull(defaultProjectRequest);
    requireNonNull(defaultProjectRequest.getString("version"), "A default Vert.x must be defined");
    this.eventBus = eventBus;
    this.defaultProjectRequest = DEFAULT_PROJECT_REQUEST.mergeIn(defaultProjectRequest);
  }

  private boolean isNotBlank(String value) {
    return value != null && value.length() > 0;
  }

  private JsonObject buildProjectRequest(HttpServerRequest request) {
    JsonObject userRequest = new JsonObject();
    String format = getArchiveFormat(request.path());
    if (format != null) {
      userRequest.put("format", format);
    }
    MultiMap params = request.params();
    if (isNotBlank(params.get("version"))) {
      userRequest.put("version", params.get("version"));
    }
    if (isNotBlank(params.get("language"))) {
      userRequest.put("language", params.get("language").toLowerCase());
    }
    if (isNotBlank(params.get("build"))) {
      userRequest.put("build", params.get("build").toLowerCase());
    }
    if (isNotBlank(params.get("groupId"))) {
      userRequest.put("groupId", params.get("groupId"));
    }
    if (isNotBlank(params.get("artifactId"))) {
      userRequest.put("artifactId", params.get("artifactId"));
    }
    if (isNotBlank(params.get("dependencies"))) {
      Set<String> dependencies = new HashSet<>(DEFAULT_DEPENDENCIES);
      for (String dependency : params.get("dependencies").split(",")) {
        dependencies.add(dependency.toLowerCase());
      }
      userRequest.put("dependencies", new JsonArray(new ArrayList(dependencies)));
    }
    return defaultProjectRequest.copy().mergeIn(userRequest);
  }

  private String getArchiveFormat(String path) {
    if (path.matches(".*\\.zip$")) {
      return "zip";
    }
    if (path.matches(".*(\\.tar\\.gz|\\.tgz)$")) {
      return "tgz";
    }
    return null;
  }


  public void create(RoutingContext rc) {
    log.debug("REST request to create Project: {}", rc.request());
    JsonObject projectRequest = buildProjectRequest(rc.request());
    eventBus.send("project.requested", projectRequest, ar -> {
      if (ar.failed()) {
        log.error("Failed to create project: {}" + ar.cause().getMessage());
        error(rc, ar.cause());
      } else {
        JsonObject metadata = (JsonObject) ar.result().body();
        String archivePath = metadata.getString("archivePath");
        File archive = new File(archivePath);
        String archiveName = projectRequest.getString("artifactId");
        rc.response()
          .setStatusCode(HTTP_OK)
          .putHeader("Content-Type", "application/zip")
          .putHeader("Content-Disposition", "attachment; filename=" + archiveName + ".zip")
          .sendFile(archive.getAbsolutePath(), onFileSent -> {
            if (onFileSent.failed()) {
              log.error("Error: {}", onFileSent.cause().getMessage());
            }
            log.info("Project created:  {}", metadata);
            eventBus.publish("project.created", metadata);
          });
      }
    });

  }

}
