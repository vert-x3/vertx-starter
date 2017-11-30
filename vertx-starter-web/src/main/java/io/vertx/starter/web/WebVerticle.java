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
package io.vertx.starter.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.starter.web.rest.DependencyResource;
import io.vertx.starter.web.rest.ProjectResource;
import io.vertx.starter.web.rest.VersionResource;
import io.vertx.starter.web.service.DependencyService;
import io.vertx.starter.web.service.ProjectService;
import io.vertx.starter.web.service.VersionService;

public class WebVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(WebVerticle.class);
    public static final int DEFAULT_HTTP_PORT = 8080;

    private VersionResource versionResource;
    private DependencyResource dependencyResource;
    private ProjectResource projectResource;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        versionResource = new VersionResource(new VersionService());
        dependencyResource = new DependencyResource(new DependencyService(config().getString("dependencies.path")));
        projectResource = new ProjectResource(config().getJsonObject("project.request"), new ProjectService(vertx.eventBus()));

        Router router = Router.router(vertx);
        cors(router);
        router
            .mountSubRouter("/api", starterRouter())
            .mountSubRouter("/api", versionsRouter())
            .mountSubRouter("/api", dependenciesRouter())
            .mountSubRouter("/api", versionsRouter());
        router.route().handler(StaticHandler.create());

        int port = config().getInteger("http.port", DEFAULT_HTTP_PORT);
        vertx
            .createHttpServer()
            .requestHandler(router::accept)
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

    private Router dependenciesRouter() {
        Router router = Router.router(vertx);
        router.get("/dependencies").handler(dependencyResource::findAll);
        return router;
    }

    private Router versionsRouter() {
        Router router = Router.router(vertx);
        router.get("/versions").handler(versionResource::findAll);
        return router;
    }

    private Router starterRouter() {
        Router router = Router.router(vertx);
        router.get("/starter*").handler(projectResource::create);
        return router;
    }

}
