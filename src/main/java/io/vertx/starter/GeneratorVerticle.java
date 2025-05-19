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

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.starter.config.Topics;
import io.vertx.starter.model.VertxProject;
import io.vertx.starter.service.GeneratorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;

public class GeneratorVerticle extends VerticleBase {

  private static final Logger log = LogManager.getLogger(GeneratorVerticle.class);

  private static final Pattern NEWLINE_REGEX = Pattern.compile("\\r?\\n");

  private GeneratorService generatorService;

  @Override
  public Future<?> start() throws Exception {
    return vertx.fileSystem().readFile("keywords").compose(res -> {
      Set<String> keywords = NEWLINE_REGEX.splitAsStream(res.toString())
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .collect(toSet());

      generatorService = new GeneratorService(vertx, keywords);

      MessageConsumer<VertxProject> consumer = vertx.eventBus().consumer(Topics.PROJECT_REQUESTED);
      return consumer.handler(this::onProjectRequested).completion();
    }).onSuccess(v -> {
      log.info("""

        ----------------------------------------------------------
        {} is running!
        ----------------------------------------------------------
        """, GeneratorVerticle.class.getSimpleName());
    });
  }

  private void onProjectRequested(Message<VertxProject> msg) {
    VertxProject project = msg.body();
    vertx.executeBlocking(() -> generatorService.onProjectRequested(project), false).onComplete(ar -> {
      if (ar.succeeded()) {
        msg.reply(ar.result());
      } else {
        log.error("Failed to generate project " + project.getId(), ar.cause());
        msg.fail(-1, ar.cause().getMessage());
      }
    });
  }
}
