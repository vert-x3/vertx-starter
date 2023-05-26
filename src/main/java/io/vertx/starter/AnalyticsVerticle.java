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
import io.vertx.core.Promise;
import io.vertx.starter.config.Topics;
import io.vertx.starter.model.VertxProject;
import io.vertx.starter.service.AnalyticsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.vertx.starter.config.VerticleConfigurationConstants.Analytics.ANALYTICS_DIR_CONF;
import static io.vertx.starter.config.VerticleConfigurationConstants.Analytics.ANALYTICS_DIR_ENV;

public class AnalyticsVerticle extends AbstractVerticle {

  private static final Logger log = LogManager.getLogger(AnalyticsVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) {
    String analyticsDirStr = config().getString(ANALYTICS_DIR_CONF, System.getenv(ANALYTICS_DIR_ENV));
    if (analyticsDirStr == null) {
      startPromise.fail("analyticsDir is null");
      return;
    }

    Path analyticsDir = Paths.get(analyticsDirStr).toAbsolutePath();
    if (!Files.isDirectory(analyticsDir)) {
      startPromise.fail(analyticsDir + " is not a directory");
      return;
    }

    try {
      Path test = Files.createTempFile(analyticsDir, "test", ".donotanalyze");
      Files.delete(test);
    } catch (IOException e) {
      startPromise.fail(new RuntimeException("Cannot write to " + analyticsDir, e));
      return;
    }

    AnalyticsService analyticsService = new AnalyticsService(vertx, analyticsDir);
    vertx.eventBus().<VertxProject>consumer(Topics.PROJECT_CREATED).handler(analyticsService::onProjectCreated);

    log.info(
      "\n----------------------------------------------------------\n\t" +
        "{} is running!\n" +
        "----------------------------------------------------------",
      AnalyticsVerticle.class.getSimpleName()
    );

    startPromise.complete();
  }
}
