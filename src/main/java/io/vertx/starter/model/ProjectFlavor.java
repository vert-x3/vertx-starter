package io.vertx.starter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Locale;

import static io.vertx.starter.config.ProjectConstants.MUTINY_FLAVOR;
import static io.vertx.starter.config.ProjectConstants.VERTX_FLAVOR;

public enum ProjectFlavor {
  @JsonProperty(VERTX_FLAVOR)
  VERTX(VERTX_FLAVOR, "io.vertx", ""),
  @JsonProperty(MUTINY_FLAVOR)
  MUTINY(MUTINY_FLAVOR, "io.smallrye.reactive", "smallrye-mutiny-");

  private final String id;
  private final String groupId;
  private final String artifactIdPrefix;

  ProjectFlavor(String id, String groupId, String artifactIdPrefix) {
    this.id = id;
    this.groupId = groupId;
    this.artifactIdPrefix = artifactIdPrefix;
  }

  public String getId() {
    return id;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getArtifactIdPrefix() {
    return artifactIdPrefix;
  }

  public static ProjectFlavor fromId(String id) {
    switch (id.toLowerCase(Locale.ROOT)) {
      case MUTINY_FLAVOR:
        return MUTINY;
      case VERTX_FLAVOR:
        return VERTX;
      default:
        return null;
    }
  }
}
