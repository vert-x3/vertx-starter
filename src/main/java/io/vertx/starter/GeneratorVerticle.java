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
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.starter.config.Topics;
import io.vertx.starter.service.GeneratorService;

public class GeneratorVerticle extends AbstractVerticle {

  private final Logger log = LoggerFactory.getLogger(GeneratorVerticle.class);

  @Override
  public void start(Future<Void> startFuture) {
    GeneratorService generatorService = new GeneratorService(vertx);
    vertx.eventBus().<JsonObject>consumer(Topics.PROJECT_REQUESTED).handler(msg -> {
      vertx.executeBlocking(fut -> {
        try {
          fut.complete(generatorService.onProjectRequested(msg));
        } catch (Exception e) {
          fut.fail(e);
        }
      }, false, ar -> {
        if (ar.succeeded()) {
          msg.reply(ar.result());
        } else {
          log.warn("Failed to generate project", ar.cause());
          msg.fail(-1, ar.cause().getMessage());
        }
      });
    });

    log.info(
      "\n----------------------------------------------------------\n\t" +
        "{} is running!\n" +
        "----------------------------------------------------------",
      GeneratorVerticle.class.getSimpleName()
    );

    startFuture.complete();
  }
}
