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

package io.vertx.starter.service;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.starter.model.BuildTool;
import io.vertx.starter.model.JdkVersion;
import io.vertx.starter.model.Language;
import io.vertx.starter.model.ProjectFlavor;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author Thomas Segismont
 */
public class MetadataHandler implements Handler<RoutingContext> {

  private final Buffer metadata;

  public MetadataHandler(JsonObject defaults, JsonArray versions, JsonArray stack) {
    metadata = new JsonObject()
      .put("defaults", defaults)
      .put("versions", versions)
      .put("stack", stack)
      .put("buildTools", values(BuildTool.values(), BuildTool::getValue))
      .put("languages", values(Language.values(), Language::getName))
      .put("jdkVersions", values(JdkVersion.values(), JdkVersion::getValue))
      .put("flavors", values(ProjectFlavor.values(), ProjectFlavor::getId))
      .put("vertxDependencies", stack) // deprecated
      .put("vertxVersions", versions.stream() // deprecated
        .map(JsonObject.class::cast)
        .map(obj -> obj.getString("number"))
        .<JsonArray>collect(JsonArray::new, JsonArray::add, JsonArray::addAll))
      .toBuffer();
  }

  private <T extends Enum> JsonArray values(T[] values, Function<T, String> toString) {
    return Arrays.stream(values).map(toString).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
  }

  @Override
  public void handle(RoutingContext rc) {
    rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(metadata);
  }
}
