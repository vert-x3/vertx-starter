package io.vertx.starter.generator.domain.impl;

import io.vertx.starter.generator.domain.ProjectFile;
import io.vertx.starter.generator.domain.ProjectFiles;

import java.util.stream.Stream;

import static io.vertx.starter.generator.domain.ProjectFile.file;


public class BasicProjectFiles implements ProjectFiles {

  @Override
  public Stream<ProjectFile> files() {
    return Stream.of(
      file("gitignore", ".gitignore"),
      file("editorconfig", ".editorconfig")
    );
  }
}
