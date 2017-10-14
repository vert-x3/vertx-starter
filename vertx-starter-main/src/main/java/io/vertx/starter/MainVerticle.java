/*
 * Copyright (c) 2017 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.starter.analytics.AnalyticsVerticle;
import io.vertx.starter.generator.GeneratorVerticle;
import io.vertx.starter.web.WebVerticle;

import static io.vertx.core.Future.future;
import static java.util.Arrays.asList;

public class MainVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(MainVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Future<String> generatorFuture = future();
        vertx.deployVerticle(
            GeneratorVerticle.class.getName(),
            new DeploymentOptions().setConfig(config().getJsonObject("generator")),
            generatorFuture
        );
        Future<String> analyticsFuture = future();
        vertx.deployVerticle(
          AnalyticsVerticle.class.getName(),
          new DeploymentOptions().setConfig(config().getJsonObject("analytics")),
          generatorFuture
        );
        Future<String> webFuture = future();
        vertx.deployVerticle(
            WebVerticle.class.getName(),
            new DeploymentOptions().setConfig(config().getJsonObject("web")),
            webFuture
        );

        CompositeFuture.all(asList(generatorFuture, analyticsFuture, webFuture)).setHandler(ar -> {
            if (ar.failed()) {
                log.error("Vertx starter failed to start: {}", ar.cause().getMessage());
                ar.cause().printStackTrace();
            } else {
                log.info("\n----------------------------------------------------------\n\t" +
                        "{} is running!\n" +
                        "----------------------------------------------------------",
                    MainVerticle.class.getSimpleName());
                startFuture.complete();
            }
        });
    }
}
