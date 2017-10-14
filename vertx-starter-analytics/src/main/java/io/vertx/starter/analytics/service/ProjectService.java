package io.vertx.starter.analytics.service;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.starter.analytics.repository.ProjectRepository;

import java.time.Instant;
import java.util.Objects;

public class ProjectService {

  private final Logger log = LoggerFactory.getLogger(ProjectService.class);

  private final Vertx vertx;
  private final ProjectRepository projectRepository;


  public ProjectService(Vertx vertx, ProjectRepository projectRepository) {
    this.vertx = vertx;
    this.projectRepository = projectRepository;
  }

  public void onProjectCreated(Message<JsonObject> message) {
    Objects.requireNonNull(message.body(), "Project can't be null");
    projectRepository.save(toProject(message.body()));
  }

  private JsonObject toProject(JsonObject projectRequest) {
    return new JsonObject()
      .put("version", projectRequest.getString("artifactId"))
      .put("language", projectRequest.getString("language"))
      .put("build", projectRequest.getString("build"))
      .put("groupId", projectRequest.getString("groupId"))
      .put("artifactId", projectRequest.getString("artifactId"))
      .put("dependencies", projectRequest.getJsonArray("dependencies"))
      .put("format", projectRequest.getString("format"))
      .put("creationDate", Instant.now());
  }

}
