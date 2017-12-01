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
package io.vertx.starter.web.rest;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.starter.web.service.VersionService;

import static io.vertx.starter.web.util.RestUtil.error;
import static io.vertx.starter.web.util.RestUtil.respondJson;

public class VersionResource {

  private final Logger log = LoggerFactory.getLogger(VersionResource.class);

  private final VersionService versionService;

  public VersionResource(VersionService versionService) {
    this.versionService = versionService;
  }

  public void findAll(RoutingContext rc) {
    log.debug("REST request to get all Versions");
    versionService.findAll(reply -> {
      if (reply.succeeded()) {
        respondJson(rc, reply.result());
      } else {
        error(rc, reply.cause());
      }
    });
  }
}
