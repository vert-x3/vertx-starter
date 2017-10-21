package io.vertx.starter.generator.domain.impl;

import io.vertx.starter.generator.domain.ProjectFile;
import io.vertx.starter.generator.domain.ProjectFiles;

import java.util.stream.Stream;

import static io.vertx.starter.generator.domain.ProjectFile.file;


public class MavenBuildProjectFiles implements ProjectFiles {

  public Stream<ProjectFile> files() {
    return Stream.of(file("pom.xml", "pom.xml"));
  }

}
