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

package io.vertx.starter;

import io.netty.util.AsciiString;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.AllowForwardHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CSPHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.HSTSHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.XFrameHandler;
import io.vertx.starter.model.VertxProject;
import io.vertx.starter.service.MetadataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.vertx.starter.config.VerticleConfigurationConstants.Web.*;

/**
 * @author Daniel Petisme
 * @author Thomas Segismont
 */
public class WebVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(WebVerticle.class);

  private static final AsciiString X_CONTENT_TYPE_OPTIONS_HEADER = AsciiString.cached("x-content-type-options");
  private static final AsciiString NOSNIFF = AsciiString.cached("nosniff");

  public static final String VERTX_PROJECT_KEY = "vertxProject";

  private final GenerationHandler generationHandler;
  private final ValidationHandler validationHandler;
  private final MetadataHandler metadataHandler;

  public WebVerticle() {
    try {

      JsonObject starterData = Util.loadStarterData();

      JsonObject defaults = starterData.getJsonObject("defaults");
      JsonArray versions = starterData.getJsonArray("versions");
      JsonArray stack = starterData.getJsonArray("stack");

      validationHandler = new ValidationHandler(defaults, versions, stack);
      generationHandler = new GenerationHandler();
      metadataHandler = new MetadataHandler(defaults, versions, stack);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.eventBus().registerDefaultCodec(VertxProject.class, new VertxProjectCodec());

    Router router = Router.router(vertx).allowForward(AllowForwardHeaders.X_FORWARD);

    router.route()
      .handler(HSTSHandler.create())
      .handler(XFrameHandler.create(XFrameHandler.DENY))
      .handler(CSPHandler.create()
        .addDirective("style-src", "self")
        .addDirective("style-src", "unsafe-inline")
        .addDirective("style-src", "cdnjs.cloudflare.com")
        .addDirective("style-src", "maxcdn.bootstrapcdn.com")
        .addDirective("style-src", "fonts.googleapis.com")
        .addDirective("font-src", "self")
        .addDirective("font-src", "maxcdn.bootstrapcdn.com")
        .addDirective("font-src", "fonts.googleapis.com")
        .addDirective("font-src", "fonts.gstatic.com")
        .addDirective("script-src", "self")
        .addDirective("script-src", "cdnjs.cloudflare.com"))
      .handler(rc -> {
        rc.response().putHeader(X_CONTENT_TYPE_OPTIONS_HEADER, NOSNIFF);
        rc.next();
      });

    CorsHandler corsHandler = CorsHandler.create("*")
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
      .allowedHeader("Content-Type")
      .allowedHeader("Accept");
    router.route().handler(corsHandler);

    router.get("/metadata").handler(metadataHandler);

    router.get("/starter.*").handler(validationHandler).handler(generationHandler);

    router.route().handler(StaticHandler.create());

    int port = config().getInteger(HTTP_PORT, 8080);

    vertx.createHttpServer()
      .requestHandler(router)
      .listen(port, ar -> {
        if (ar.failed()) {
          log.error("Fail to start {}", WebVerticle.class.getSimpleName(), ar.cause());
          startPromise.fail(ar.cause());
        } else {
          log.info("\n----------------------------------------------------------\n\t" +
              "{} is running! Access URLs:\n\t" +
              "Local: \t\thttp://localhost:{}\n" +
              "----------------------------------------------------------",
            WebVerticle.class.getSimpleName(), port);
          startPromise.complete();
        }
      });
  }

  static void fail(RoutingContext rc, int status, String message) {
    JsonObject error = new JsonObject()
      .put("status", status)
      .put("message", message);
    rc.response().setStatusCode(status).putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(error.toBuffer());
  }
}
