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
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.starter.config.Topics;
import io.vertx.starter.model.VertxProject;
import io.vertx.starter.service.GeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;

public class GeneratorVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(GeneratorVerticle.class);

  private static final Pattern NEWLINE_REGEX = Pattern.compile("\\r?\\n");

  private GeneratorService generatorService;

  @Override
  public void start(Future<Void> startFuture) {
    vertx.fileSystem().readFile("keywords", kar -> {
      if (kar.succeeded()) {

        Set<String> keywords = NEWLINE_REGEX.splitAsStream(kar.result().toString())
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .collect(toSet());

        generatorService = new GeneratorService(vertx, keywords);

        MessageConsumer<JsonObject> consumer = vertx.eventBus().consumer(Topics.PROJECT_REQUESTED);
        consumer.handler(this::onProjectRequested).completionHandler(ar -> {
          if (ar.succeeded()) {

            log.info(
              "\n----------------------------------------------------------\n\t" +
                "{} is running!\n" +
                "----------------------------------------------------------",
              GeneratorVerticle.class.getSimpleName()
            );

            startFuture.complete();

          } else {
            startFuture.fail(ar.cause());
          }
        });

      } else {
        startFuture.fail(kar.cause());
      }
    });
  }

  private void onProjectRequested(Message<JsonObject> msg) {
    VertxProject vertxProject = msg.body().mapTo(VertxProject.class);
    vertx.executeBlocking(fut -> {
      try {
        fut.complete(generatorService.onProjectRequested(vertxProject));
      } catch (Exception e) {
        fut.fail(e);
      }
    }, false, ar -> {
      if (ar.succeeded()) {
        msg.reply(ar.result());
      } else {
        log.error("Failed to generate project " + vertxProject.getId(), ar.cause());
        msg.fail(-1, ar.cause().getMessage());
      }
    });
  }
}
