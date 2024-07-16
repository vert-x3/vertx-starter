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

import io.netty.util.AsciiString;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.WebEnvironment;
import io.vertx.starter.model.BuildTool;
import io.vertx.starter.model.JdkVersion;
import io.vertx.starter.model.Language;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.function.Function;

import static io.vertx.core.http.HttpHeaders.CACHE_CONTROL;

/**
 * @author Thomas Segismont
 */
public class MetadataHandler implements Handler<RoutingContext> {

  private static final AsciiString ONE_DAY_CACHE = AsciiString.cached("cache-control=public, max-age=86400");

  private final Buffer metadata;
  private final String etag;

  public MetadataHandler(JsonObject defaults, JsonArray versions, JsonArray stack) {
    metadata = new JsonObject()
      .put("defaults", defaults)
      .put("versions", versions)
      .put("stack", stack)
      .put("buildTools", values(BuildTool.values(), BuildTool::getValue))
      .put("languages", values(Language.values(), Language::getName))
      .put("jdkVersions", values(JdkVersion.values(), JdkVersion::getValue))
      .put("vertxDependencies", stack) // deprecated
      .put("vertxVersions", versions.stream() // deprecated
        .map(JsonObject.class::cast)
        .map(obj -> obj.getString("number"))
        .collect(JsonArray::new, JsonArray::add, JsonArray::addAll))
      .toBuffer();

    etag = computeEtag(metadata);
  }

  private static String computeEtag(Buffer metadata) {
    try {
      return "\"" + encode(digest(metadata)) + "\"";
    } catch (NoSuchAlgorithmException e) {
      return null;
    }
  }

  private static String encode(byte[] digest) {
    return HexFormat.of().formatHex(digest);
  }

  private static byte[] digest(Buffer metadata) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(metadata.getBytes());
    return md.digest();
  }

  private <T extends Enum<?>> JsonArray values(T[] values, Function<T, String> toString) {
    return Arrays.stream(values).map(toString).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
  }

  @Override
  public void handle(RoutingContext rc) {
    HttpServerResponse response = rc.response();
    if (!WebEnvironment.development()) {
      response.putHeader(CACHE_CONTROL, ONE_DAY_CACHE);
      if (etag != null) {
        rc.etag(etag);
        if (rc.isFresh()) {
          response.setStatusCode(304).end();
          return;
        }
      }
    }
    response.putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(metadata);
  }
}
