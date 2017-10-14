package io.vertx.starter.analytics;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.starter.analytics.repository.ProjectRepository;
import io.vertx.starter.analytics.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyticsVerticle extends AbstractVerticle {

  private final Logger log = LoggerFactory.getLogger(AnalyticsVerticle.class);

  private MongoClient mongoClient() {
    return MongoClient.createShared(vertx, config());
  }

  private ProjectService projectService() {
    return new ProjectService(vertx, new ProjectRepository(mongoClient()));
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    ProjectService projectService = projectService();
    vertx.eventBus().<JsonObject>consumer("project.created").handler(projectService::onProjectCreated);

    log.info("\n----------------------------------------------------------\n\t" +
        "{} is running!\n" +
        "----------------------------------------------------------",
      AnalyticsVerticle.class.getSimpleName());
    startFuture.complete();
  }
}
