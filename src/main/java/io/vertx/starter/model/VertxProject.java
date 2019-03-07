/*
 * Copyright (c) 2017-2018 Daniel Petisme
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.starter.model;

import java.util.Set;

public class VertxProject {

  private String id;
  private String groupId;
  private String artifactId;
  private Language language;
  private BuildTool buildTool;
  private String vertxVersion;
  private Set<String> vertxDependencies;
  private ArchiveFormat archiveFormat;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  public BuildTool getBuildTool() {
    return buildTool;
  }

  public void setBuildTool(BuildTool buildTool) {
    this.buildTool = buildTool;
  }

  public String getVertxVersion() {
    return vertxVersion;
  }

  public void setVertxVersion(String vertxVersion) {
    this.vertxVersion = vertxVersion;
  }

  public Set<String> getVertxDependencies() {
    return vertxDependencies;
  }

  public void setVertxDependencies(Set<String> vertxDependencies) {
    this.vertxDependencies = vertxDependencies;
  }

  public ArchiveFormat getArchiveFormat() {
    return archiveFormat;
  }

  public void setArchiveFormat(ArchiveFormat archiveFormat) {
    this.archiveFormat = archiveFormat;
  }

  @Override
  public String toString() {
    return "Project{" +
      "id='" + id + '\'' +
      ", groupId='" + groupId + '\'' +
      ", artifactId='" + artifactId + '\'' +
      ", language=" + language +
      ", buildTool=" + buildTool +
      ", vertxVersion='" + vertxVersion + '\'' +
      ", vertxDependencies=" + vertxDependencies +
      ", archiveFormat=" + archiveFormat +
      '}';
  }
}
