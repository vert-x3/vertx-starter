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
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.starter.model.*;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static io.vertx.starter.config.ProjectConstants.*;
import static java.util.stream.Collectors.*;

/**
 * @author Daniel Petisme
 * @author Thomas Segismont
 */
public class ValidationHandler implements Handler<RoutingContext> {

  private static final Pattern ID_REGEX = Pattern.compile("^[A-Za-z0-9_\\-.]+$");

  private final JsonObject defaults;
  private final Set<String> versions;
  private final Map<String, List<String>> exclusions;
  private final Set<String> dependencies;

  public ValidationHandler(JsonObject defaults, JsonArray versions, JsonArray stack) {
    this.defaults = defaults;
    this.versions = versions.stream()
      .map(JsonObject.class::cast)
      .map(obj -> obj.getString("number"))
      .collect(toSet());
    exclusions = versions.stream()
      .map(JsonObject.class::cast)
      .collect(toMap(
        obj -> obj.getString("number"),
        obj -> obj.getJsonArray("exclusions", new JsonArray()).stream().map(String.class::cast).collect(toList()))
      );
    dependencies = stack.stream()
      .map(JsonObject.class::cast)
      .flatMap(category -> category.getJsonArray("items").stream())
      .map(JsonObject.class::cast)
      .map(item -> item.getString("artifactId"))
      .collect(toSet());
  }

  @Override
  public void handle(RoutingContext rc) {
    VertxProject vertxProject = defaults.mapTo(VertxProject.class);

    vertxProject.setId(UUID.randomUUID().toString());

    if (!validateAndSetId(rc, GROUP_ID, vertxProject::setGroupId)) {
      return;
    }
    if (!validateAndSetId(rc, ARTIFACT_ID, vertxProject::setArtifactId)) {
      return;
    }

    if (!validateAndSetEnum(rc, LANGUAGE, Language::fromString, vertxProject::setLanguage)) {
      return;
    }

    if (!validateAndSetEnum(rc, BUILD_TOOL, BuildTool::fromString, vertxProject::setBuildTool)) {
      return;
    }

    String vertxVersion = getQueryParam(rc, VERTX_VERSION);
    if (isNotBlank(vertxVersion)) {
      if (!versions.contains(vertxVersion)) {
        fail(rc, VERTX_VERSION, vertxVersion);
        return;
      }
      vertxProject.setVertxVersion(vertxVersion);
    }

    String deps = getQueryParam(rc, VERTX_DEPENDENCIES);
    if (isNotBlank(deps)) {
      Set<String> vertxDependencies = Arrays.stream(deps.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(String::toLowerCase)
        .collect(toSet());

      if (!dependencies.containsAll(vertxDependencies) ||
        !Collections.disjoint(exclusions.get(vertxProject.getVertxVersion()), vertxDependencies)) {
        fail(rc, VERTX_DEPENDENCIES, deps);
        return;
      }

      if (vertxDependencies.contains("vertx-unit") && vertxDependencies.contains("vertx-junit5")) {
        WebVerticle.fail(rc, 400, "You cannot generate a project which depends on both vertx-unit and vertx-junit5.");
        return;
      }

      vertxProject.setVertxDependencies(vertxDependencies);
    }

    ArchiveFormat archiveFormat = ArchiveFormat.fromFilename(rc.request().path());
    if (archiveFormat != null) {
      vertxProject.setArchiveFormat(archiveFormat);
    } else {
      fail(rc, ARCHIVE_FORMAT, rc.request().path());
      return;
    }

    if (!validateAndSetId(rc, PACKAGE_NAME, vertxProject::setPackageName)) {
      return;
    }

    if (!validateAndSetEnum(rc, JDK_VERSION, JdkVersion::fromString, vertxProject::setJdkVersion)) {
      return;
    }

    vertxProject.setOperatingSystem(operatingSystem(rc.request().getHeader(HttpHeaders.USER_AGENT)));

    vertxProject.setCreatedOn(Instant.now());

    rc.put(WebVerticle.VERTX_PROJECT_KEY, vertxProject);
    rc.next();
  }

  private boolean validateAndSetId(RoutingContext rc, String name, Consumer<String> setter) {
    String value = getQueryParam(rc, name);
    if (isNotBlank(value)) {
      if (!ID_REGEX.matcher(value).matches()) {
        fail(rc, name, value);
        return false;
      }
      setter.accept(value);
    }
    return true;
  }

  private <T extends Enum> boolean validateAndSetEnum(RoutingContext rc, String name, Function<String, T> factory, Consumer<T> setter) {
    String value = getQueryParam(rc, name);
    if (isNotBlank(value)) {
      T t = factory.apply(value);
      if (t == null) {
        fail(rc, name, value);
        return false;
      }
      setter.accept(t);
    }
    return true;
  }

  private String getQueryParam(RoutingContext rc, String name) {
    String value = rc.queryParams().get(name);
    return value == null ? null : value.trim();
  }

  private boolean isNotBlank(String value) {
    return value != null && value.length() > 0;
  }

  private String operatingSystem(String userAgentHeader) {
    if (userAgentHeader != null) {
      String ua = userAgentHeader.toLowerCase();
      if (ua.contains("macintosh")) {
        return "Mac";
      }
      if (ua.contains("windows")) {
        return "Windows";
      }
    }
    return "Other";
  }

  private void fail(RoutingContext rc, String name, String value) {
    WebVerticle.fail(rc, 400, String.format("Invalid value %s of param %s", value, name));
  }
}
