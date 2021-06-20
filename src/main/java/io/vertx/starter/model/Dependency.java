package io.vertx.starter.model;

import java.util.Objects;

public class Dependency {
  private String groupId;
  private String artifactId;

  public String getGroupId() {
    return groupId;
  }

  public Dependency setGroupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public Dependency setArtifactId(String artifactId) {
    this.artifactId = artifactId;
    return this;
  }

  public boolean isVertxDependency() {
    return ProjectFlavor.VERTX.getGroupId().equals(groupId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Dependency that = (Dependency) o;
    return Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, artifactId);
  }

  @Override
  public String toString() {
    return "Dependency{" +
      "groupId='" + groupId + '\'' +
      ", artifactId='" + artifactId + '\'' +
      '}';
  }
}
