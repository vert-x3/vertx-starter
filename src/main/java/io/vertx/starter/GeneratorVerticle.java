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
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.starter.config.Topics;
import io.vertx.starter.model.VertxProject;
import io.vertx.starter.service.GeneratorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;

public class GeneratorVerticle extends AbstractVerticle {

  private static final Logger log = LogManager.getLogger(GeneratorVerticle.class);

  private static final Pattern NEWLINE_REGEX = Pattern.compile("\\r?\\n");

  private GeneratorService generatorService;

  @Override
  public void start() throws Exception {
    var buffer = vertx.fileSystem().readFile("keywords").await();
    var keywords = NEWLINE_REGEX.splitAsStream(buffer.toString())
      .map(String::trim)
      .filter(s -> !s.isEmpty())
      .collect(toSet());

    generatorService = new GeneratorService(vertx, keywords);

    MessageConsumer<VertxProject> consumer = vertx.eventBus().consumer(Topics.PROJECT_REQUESTED);
    consumer.handler(this::onProjectRequested).completion().await();

    log.info("""

      ----------------------------------------------------------
      {} is running!
      ----------------------------------------------------------
      """, GeneratorVerticle.class.getSimpleName());
  }

  private void onProjectRequested(Message<VertxProject> msg) {
    var project = msg.body();
    try {
      msg.reply(generatorService.onProjectRequested(project));
    } catch (Exception e) {
      log.error("Failed to generate project {}", project.getId(), e);
      msg.fail(-1, e.getMessage());
    }
  }
}
