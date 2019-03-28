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
import io.vertx.ext.mongo.MongoClient;
import io.vertx.starter.config.Topics;
import io.vertx.starter.model.VertxProject;
import io.vertx.starter.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(AnalyticsVerticle.class);

  private MongoClient mongoClient() {
    return MongoClient.createShared(vertx, config());
  }

  @Override
  public void start(Future<Void> startFuture) {
    AnalyticsService analyticsService = new AnalyticsService(mongoClient());
    vertx.eventBus().<VertxProject>consumer(Topics.PROJECT_CREATED).handler(analyticsService::onProjectCreated);

    log.info(
      "\n----------------------------------------------------------\n\t" +
        "{} is running!\n" +
        "----------------------------------------------------------",
      AnalyticsVerticle.class.getSimpleName()
    );

    startFuture.complete();
  }
}
