package io.vertx.starter.generator.domain.impl;

import io.vertx.starter.generator.domain.BuildTool;
import io.vertx.starter.generator.domain.ProjectFile;
import io.vertx.starter.generator.domain.ProjectFiles;
import io.vertx.starter.generator.domain.ProjectUtils;

import java.util.stream.Stream;

import static io.vertx.starter.generator.domain.ProjectFile.file;

public class MavenJavaProjectFiles implements ProjectFiles {

  private final String groupId;
  private final String artifactId;
  private final BuildTool buildTool = new BuildTool.Maven("java");

  public MavenJavaProjectFiles(String groupId, String artifactId) {
    this.groupId = groupId;
    this.artifactId = artifactId;
  }

  @Override
  public Stream<ProjectFile> files() {
    String packageDir = ProjectUtils.packageDir(groupId, artifactId);
    String mainPackageDir = buildTool.mainSourcesDir() + packageDir;
    return Stream.of(
      file(buildTool.mainSourcesDir() + "package/MainVerticle.java", mainPackageDir + "MainVerticle.java")
    );
  }

}
