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
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.starter.service.StarterMetadataService;
import io.vertx.starter.web.rest.StarterResource;

import static io.vertx.starter.config.VerticleConfigurationConstants.Web.HTTP_PORT;

public class WebVerticle extends AbstractVerticle {

  public static final int DEFAULT_HTTP_PORT = 8080;
  private final Logger log = LoggerFactory.getLogger(WebVerticle.class);

  private StarterResource starterResource;

  @Override
  public void start(Future<Void> startFuture) {
    starterResource = new StarterResource(
      this.vertx.eventBus(),
      new StarterMetadataService(this.vertx),
      config().getJsonObject("project-defaults")
    );

    Router router = Router.router(vertx);
    cors(router);
    router.get("/starter.*").handler(starterResource::generateProject);
    router.get("/metadata").handler(starterResource::getStarterMetadata);
    router.route().produces("text/html").handler(StaticHandler.create());

    int port = config().getInteger(HTTP_PORT, DEFAULT_HTTP_PORT);
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(port, ar -> {
        if (ar.failed()) {
          log.error("Fail to start {}: {}", WebVerticle.class.getSimpleName(), ar.cause().getMessage());
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

  private void cors(Router router) {
    router.route().handler(CorsHandler.create("*")
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
      .allowedHeader("Content-Type")
      .allowedHeader("Accept")
    );
  }

}
