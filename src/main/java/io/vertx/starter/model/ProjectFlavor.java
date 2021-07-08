package io.vertx.starter.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum ProjectFlavor {
  VERTX("vert.x", "io.vertx"),
  MUTINY("mutiny", "io.smallrye.reactive");

  private final String id;
  private final String groupId;

  ProjectFlavor(String id, String groupId) {
    this.id = id;
    this.groupId = groupId;
  }

  @JsonValue
  public String getId() {
    return id;
  }

  public String getGroupId() {
    return groupId;
  }

  @JsonIgnore
  public String capitalizedId() {
    return id.substring(0, 1).toUpperCase(Locale.ROOT) + id.substring(1);
  }

  public static ProjectFlavor fromId(String id) {
    switch (id.toLowerCase(Locale.ROOT)) {
      case "mutiny":
        return MUTINY;
      case "vert.x":
        return VERTX;
      default:
        return null;
    }
  }
}
