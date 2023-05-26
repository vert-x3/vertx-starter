/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.starter;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.starter.model.VertxProject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.vertx.starter.config.Topics.PROJECT_CREATED;
import static io.vertx.starter.config.Topics.PROJECT_REQUESTED;

/**
 * @author Daniel Petisme
 * @author Thomas Segismont
 */
public class GenerationHandler implements Handler<RoutingContext> {

  private static final Logger log = LogManager.getLogger(GenerationHandler.class);

  @Override
  public void handle(RoutingContext rc) {
    VertxProject project = rc.get(WebVerticle.VERTX_PROJECT_KEY);

    rc.vertx().eventBus().<Buffer>request(PROJECT_REQUESTED, project, reply -> {
      if (reply.succeeded()) {

        rc.vertx().eventBus().publish(PROJECT_CREATED, project);

        Buffer content = reply.result().body();
        String filename = project.getArtifactId() + "." + project.getArchiveFormat().getFileExtension();

        rc.response()
          .putHeader(HttpHeaders.CONTENT_ENCODING, HttpHeaders.IDENTITY)
          .putHeader(HttpHeaders.CONTENT_TYPE, project.getArchiveFormat().getContentType())
          .putHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
          .end(content);

      } else {
        log.error("Failed to create project " + project.getId(), reply.cause());
        WebVerticle.fail(rc, 500, "Failed to create project: " + project.getId());
      }
    });
  }
}
