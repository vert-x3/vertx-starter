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
package io.vertx.starter.web.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.Objects.requireNonNull;

public class DependencyService {

  private static final Logger log = LoggerFactory.getLogger(DependencyService.class);

  private JsonArray dependencies;

  public DependencyService(String dependenciesPath) {
    requireNonNull(dependenciesPath);
    try {
      String raw = new String(
        Files.readAllBytes(new File(dependenciesPath).toPath())
      );
      dependencies = new JsonObject(raw).getJsonArray("content");
    } catch (IOException e) {
      log.error("Impossible to load dependencies at path {}: {}", dependenciesPath, e.getMessage());
    }

    public void findAll(Handler<AsyncResult<JsonArray>> reply) {
        if (dependencies != null) {
            reply.handle(Future.succeededFuture(dependencies));
        } else {
            reply.handle(Future.failedFuture("Impossible to retrieve dependencies"));
        }
    }

}
