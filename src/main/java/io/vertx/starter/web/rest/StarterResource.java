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

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.starter.model.ArchiveFormat;
import io.vertx.starter.model.BuildTool;
import io.vertx.starter.model.Language;
import io.vertx.starter.model.VertxProject;
import io.vertx.starter.service.StarterMetadataService;
import io.vertx.starter.web.util.RestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.vertx.starter.config.ProjectConstants.*;
import static io.vertx.starter.config.Topics.PROJECT_CREATED;
import static io.vertx.starter.config.Topics.PROJECT_REQUESTED;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Arrays.asList;

public class StarterResource {

  private static final Logger log = LoggerFactory.getLogger(StarterResource.class);

  private final EventBus eventBus;
  private final StarterMetadataService starterMetadataService;
  private final JsonObject defaults;

  public StarterResource(EventBus eventBus, StarterMetadataService starterMetadataService, JsonObject defaults) {
    this.eventBus = eventBus;
    this.starterMetadataService = starterMetadataService;
    this.defaults = defaults;
  }

  private VertxProject createProjectWithDefaultValues() {
    return defaults.mapTo(VertxProject.class);
  }

  public void getStarterMetadata(RoutingContext rc) {
    log.debug("REST request to get starter metdata");
    JsonObject details = new JsonObject();
    details.put("defaults", defaults);
    details.put("buildTools", asList("maven", "gradle"));
    details.put("languages", asList("java", "kotlin"));
    starterMetadataService.getAllVertxDependencies(dependencies -> {
      if (dependencies.succeeded()) {
        details.put("vertxDependencies", dependencies.result());
        starterMetadataService.getAllVertxVersions(versions -> {
          if (versions.succeeded()) {
            details.put("vertxVersions", versions.result());
            RestUtil.respondJson(rc, details);
          } else {
            RestUtil.error(rc, versions.cause());
          }
        });
      } else {
        RestUtil.error(rc, dependencies.cause());
      }
    });
  }

  public void generateProject(RoutingContext rc) {
    VertxProject project = buildProject(rc.request());
    log.debug("REST request to generate project: {}", project);
    this.eventBus.<Buffer>send(PROJECT_REQUESTED, JsonObject.mapFrom(project), reply -> {
      if (reply.succeeded()) {
        Buffer content = reply.result().body();
        String filename = project.getArtifactId() + "." + project.getArchiveFormat().getFileExtension();
        log.debug("Sending archive: " + filename);
        rc.response()
          .setStatusCode(HTTP_OK)
          .putHeader("Content-Type", project.getArchiveFormat().getContentType())
          .putHeader("Content-Disposition", "attachment; filename=" + filename)
          .end(content);
        log.debug("Notifying project created");
        this.eventBus.publish(PROJECT_CREATED, JsonObject.mapFrom(project));
      } else {
        String errorMessage = reply.cause().getMessage();
        log.error("Failed to create project: {}", project.getId());
        RestUtil.error(rc, "Failed to create project: " + project.getId());
      }
    });
  }

  private VertxProject buildProject(HttpServerRequest request) {
    VertxProject project = createProjectWithDefaultValues();
    String projectId = UUID.randomUUID().toString();
    project.setId(projectId);

    MultiMap params = request.params();
    if (isNotBlank(params.get(TYPE))) {
      project.setType(params.get(TYPE));
    }
    if (isNotBlank(params.get(GROUP_ID))) {
      project.setGroupId(params.get(GROUP_ID));
    }
    if (isNotBlank(params.get(ARTIFACT_ID))) {
      project.setArtifactId(params.get(ARTIFACT_ID));
    }
    if (isNotBlank(params.get(LANGUAGE))) {
      project.setLanguage(Language.valueOf(params.get(LANGUAGE).toUpperCase()));
    }
    if (isNotBlank(params.get(BUILD_TOOL))) {
      project.setBuildTool(BuildTool.valueOf(params.get(BUILD_TOOL).toUpperCase()));
    }
    if (isNotBlank(params.get(VERTX_VERSION))) {
      project.setVertxVersion(params.get(VERTX_VERSION));
    }
    Set<String> vertxDependencies = new HashSet<>(project.getVertxDependencies());
    if (isNotBlank(params.get(VERTX_DEPENDENCIES))) {
      for (String dependency : params.get(VERTX_DEPENDENCIES).split(",")) {
        vertxDependencies.add(dependency.toLowerCase());
      }
      project.setVertxDependencies(vertxDependencies);
    }
    ArchiveFormat archiveFormat = Optional
      .ofNullable(ArchiveFormat.fromFilename(request.path()))
      .orElse(ArchiveFormat.valueOf(defaults.getString(ARCHIVE_FORMAT).toUpperCase()));
    project.setArchiveFormat(archiveFormat);
    return project;
  }

  private boolean isNotBlank(String value) {
    return value != null && value.length() > 0;
  }

}
