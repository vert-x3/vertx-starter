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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.starter.model.VertxProject;
import io.vertx.starter.service.MetadataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static io.vertx.starter.config.VerticleConfigurationConstants.Web.HTTP_PORT;

/**
 * @author Daniel Petisme
 * @author Thomas Segismont
 */
public class WebVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(WebVerticle.class);

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
  public void start(Future<Void> startFuture) {
    vertx.eventBus().registerDefaultCodec(VertxProject.class, new VertxProjectCodec());

    Router router = Router.router(vertx);

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
          startFuture.fail(ar.cause());
        } else {
          log.info("\n----------------------------------------------------------\n\t" +
              "{} is running! Access URLs:\n\t" +
              "Local: \t\thttp://localhost:{}\n" +
              "----------------------------------------------------------",
            WebVerticle.class.getSimpleName(), port);
          startFuture.complete();
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
