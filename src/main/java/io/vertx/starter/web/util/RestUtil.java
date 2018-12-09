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

package io.vertx.starter.web.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

public final class RestUtil {

  public static void error(RoutingContext rc, Throwable cause) {
    error(rc, cause.getCause().getMessage());
  }

  public static void error(RoutingContext rc, String message) {
    rc.response().setStatusCode(HTTP_INTERNAL_ERROR).end(new JsonObject().put("status", HTTP_INTERNAL_ERROR).put("message", message).toString());
  }

  private static void respond(RoutingContext rc, String contentType, String chunk) {
    rc
      .response()
      .setStatusCode(HTTP_OK)
      .putHeader("Content-Type", contentType)
      .end(chunk);
  }

  public static void respondJson(RoutingContext rc, JsonObject object) {
    respond(rc, "application/json", object.encode());
  }

  public static void respondJson(RoutingContext rc, JsonArray array) {
    respond(rc, "application/json", array.encode());
  }
}
