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

import java.time.Instant;
import java.util.Set;

public class VertxProject {

  private String id;
  private String groupId;
  private String artifactId;
  private Language language;
  private BuildTool buildTool;
  private String vertxVersion;
  private Set<Dependency> vertxDependencies;
  private ArchiveFormat archiveFormat;
  private String packageName;
  private JdkVersion jdkVersion;
  private String operatingSystem;
  private Instant createdOn;
  private ProjectFlavor flavor;

  public String getId() {
    return id;
  }

  public VertxProject setId(String id) {
    this.id = id;
    return this;
  }

  public String getGroupId() {
    return groupId;
  }

  public VertxProject setGroupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public VertxProject setArtifactId(String artifactId) {
    this.artifactId = artifactId;
    return this;
  }

  public Language getLanguage() {
    return language;
  }

  public VertxProject setLanguage(Language language) {
    this.language = language;
    return this;
  }

  public BuildTool getBuildTool() {
    return buildTool;
  }

  public VertxProject setBuildTool(BuildTool buildTool) {
    this.buildTool = buildTool;
    return this;
  }

  public String getVertxVersion() {
    return vertxVersion;
  }

  public VertxProject setVertxVersion(String vertxVersion) {
    this.vertxVersion = vertxVersion;
    return this;
  }

  public Set<Dependency> getVertxDependencies() {
    return vertxDependencies;
  }

  public VertxProject setVertxDependencies(Set<Dependency> vertxDependencies) {
    this.vertxDependencies = vertxDependencies;
    return this;
  }

  public ArchiveFormat getArchiveFormat() {
    return archiveFormat;
  }

  public VertxProject setArchiveFormat(ArchiveFormat archiveFormat) {
    this.archiveFormat = archiveFormat;
    return this;
  }

  public String getPackageName() {
    return packageName;
  }

  public VertxProject setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public JdkVersion getJdkVersion() {
    return jdkVersion;
  }

  public VertxProject setJdkVersion(JdkVersion jdkVersion) {
    this.jdkVersion = jdkVersion;
    return this;
  }

  public String getOperatingSystem() {
    return operatingSystem;
  }

  public VertxProject setOperatingSystem(String operatingSystem) {
    this.operatingSystem = operatingSystem;
    return this;
  }

  public Instant getCreatedOn() {
    return createdOn;
  }

  public VertxProject setCreatedOn(Instant createdOn) {
    this.createdOn = createdOn;
    return this;
  }

  public ProjectFlavor getFlavor() {
    return flavor;
  }

  public VertxProject setFlavor(ProjectFlavor flavor) {
    this.flavor = flavor;
    return this;
  }

  @Override
  public String toString() {
    // DO NOT RETURN USER RELATED-DATA (groupId, artifactId, packageName)
    return "VertxProject{" +
      "id='" + id + '\'' +
      ", language=" + language +
      ", buildTool=" + buildTool +
      ", vertxVersion='" + vertxVersion + '\'' +
      ", vertxDependencies=" + vertxDependencies +
      ", archiveFormat=" + archiveFormat +
      ", jdkVersion=" + jdkVersion +
      ", operatingSystem=" + operatingSystem +
      ", createdOn=" + createdOn +
      '}';
  }
}
