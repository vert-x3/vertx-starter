package io.vertx.starter.generator.domain;

import io.vertx.starter.generator.domain.impl.BasicProjectFiles;
import io.vertx.starter.generator.domain.impl.MavenJavaProjectFiles;

import java.util.stream.Stream;

public class ProjectFilesProvider {

  public static Stream<ProjectFile> projectFiles(String groupId, String artifactId, String language, String buildTool) {
    return Stream.concat(
      new BasicProjectFiles().files(),
      findProjectFiles(groupId, artifactId, language, buildTool).files()
    );
  }

  private static ProjectFiles findProjectFiles(String groupId, String artifactId, String language, String buildTool) {
    if (buildTool.equalsIgnoreCase("maven")) {
      if (language.equalsIgnoreCase("java")) {
        return new MavenJavaProjectFiles(groupId, artifactId);
      }
    }
    return null;
  }

}
