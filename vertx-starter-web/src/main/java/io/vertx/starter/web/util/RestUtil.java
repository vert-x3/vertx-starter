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
package io.vertx.starter.web.util;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

public final class RestUtil {

    public static void error(RoutingContext rc, Throwable cause) {
        rc.response().setStatusCode(HTTP_INTERNAL_ERROR).end(new JsonObject().put("status", HTTP_INTERNAL_ERROR).put("message", cause.getMessage()).toString());
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
