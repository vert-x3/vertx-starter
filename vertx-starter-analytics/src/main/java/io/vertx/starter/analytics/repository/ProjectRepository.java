package io.vertx.starter.analytics.repository;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

public class ProjectRepository {

  private static final String COLLECTION_NAME = "projects";

  private final Logger log = LoggerFactory.getLogger(ProjectRepository.class);

  private final MongoClient mongoClient;

  public ProjectRepository(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  public void save(JsonObject project) {
    mongoClient.save(COLLECTION_NAME, project, res -> {
      if (res.failed()) {
        log.error("Failed to save project {}: {}", project, res.cause().getMessage());
      } else {
        log.debug("Saved project: {}", project);
      }
    });
  }

  public void findAll(Handler<AsyncResult<List<JsonObject>>> handler) {
    mongoClient.find(COLLECTION_NAME, new JsonObject(), handler);
  }


}
