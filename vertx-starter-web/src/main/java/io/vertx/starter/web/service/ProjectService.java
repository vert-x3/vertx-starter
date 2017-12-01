package io.vertx.starter.web.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class ProjectService {

  private final EventBus eventBus;

  public ProjectService(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public void create(JsonObject request, Handler<AsyncResult<String>> reply) {
    eventBus.send("project.requested", request, ar -> {
      if (ar.succeeded()) {
        JsonObject project = (JsonObject) ar.result().body();
        eventBus.publish("project.created", project);
        reply.handle(Future.succeededFuture(project.getString("archivePath")));
      } else {
        reply.handle(Future.failedFuture(ar.cause()));
      }

    });
  }

}
