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
package io.vertx.starter.generator.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class StarterService {

    private final Logger log = LoggerFactory.getLogger(StarterService.class);

    private Vertx vertx;
    private String tempDir;

    public StarterService(Vertx vertx, String tempDir) {
        this.vertx = vertx;
        this.tempDir = tempDir;
    }

    JsonObject buildMetadata(JsonObject projectRequest) {
        Path rootDir = Paths.get(tempDir, "vertx-starter", UUID.randomUUID().toString());
        Path baseDir = rootDir.resolve(projectRequest.getString("artifactId", "project"));
        projectRequest.put("rootDir", rootDir.toString());
        projectRequest.put("baseDir", baseDir.toString());
        return projectRequest;
    }

    public void starter(Message<JsonObject> request) {
        JsonObject metadata = buildMetadata(request.body());
        log.info("Forging project with request: {}", metadata);
        createTempDir(metadata)
            .compose(v -> generate(metadata))
            .compose(v -> archive(metadata))
            .setHandler(ar -> {
                if (ar.failed()) {
                    log.error("Impossible to create project {}: {}", metadata, ar.cause().getMessage());
                    request.fail(-1, "Impossible to createProject");
                } else {
                    String archivePath = ar.result();
                    log.debug("Archive starterd: {}", archivePath);
                    metadata.put("archivePath", archivePath);
                    vertx.eventBus().publish("starter:created", metadata);
                    request.reply(metadata);
                }
            });
    }

    private Future<Void> generate(JsonObject metadata) {
        Future future = Future.future();
        vertx.eventBus().send("generate", metadata, ar -> {
            if (ar.failed()) {
                log.error(ar.cause().getMessage());
            } else {
                future.complete();
            }
        });
        return future;
    }

    private Future<String> archive(JsonObject metadata) {
        Future future = Future.future();
        vertx.eventBus().send("archive", metadata, ar -> {
            if (ar.failed()) {
                log.error(ar.cause().getMessage());
            } else {
                future.complete(ar.result().body());
            }
        });
        return future;
    }

    private Future<Void> createTempDir(JsonObject metadata) {
        Future future = Future.future();
        String dir = metadata.getString("baseDir");
        vertx.fileSystem().mkdirs(dir, ar -> {
            if (ar.failed()) {
                log.error("Impossible to create temp directory {}: {}", dir, ar.cause().getMessage());
                future.fail(ar.cause());
            } else {
                future.complete();
            }
        });
        return future;
    }


    public void clean(Message<JsonObject> message) {
        JsonObject metadata = message.body();
        String rootDir = metadata.getString("rootDir");
        vertx.fileSystem().deleteRecursive(rootDir, true, ar -> {
            if (ar.failed()) {
                log.error("Impossible to delete temp directory {}: {}", rootDir, ar.cause().getMessage());
            } else {
                log.debug("Temp directory {} deleted", rootDir);
            }
        });

    }
}
