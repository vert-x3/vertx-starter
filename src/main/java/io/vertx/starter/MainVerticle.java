/*
 * Copyright 2023 Red Hat, Inc.
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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import static io.vertx.starter.config.VerticleConfigurationConstants.Web.HTTP_PORT;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    DeploymentOptions analyticsOptions = new DeploymentOptions()
      .setConfig(new JsonObject()
        .put("host", "localhost")
        .put("port", 27017)
        .put("db_name", "vertx-starter-analytics"));

    DeploymentOptions webOptions = new DeploymentOptions()
      .setConfig(new JsonObject().put(HTTP_PORT, 8080));

    vertx.deployVerticle(AnalyticsVerticle::new, analyticsOptions)
      .compose(v -> vertx.deployVerticle(GeneratorVerticle::new, new DeploymentOptions()))
      .compose(v -> vertx.deployVerticle(WebVerticle::new, webOptions))
      .<Void>mapEmpty()
      .onComplete(startPromise);
  }
}
