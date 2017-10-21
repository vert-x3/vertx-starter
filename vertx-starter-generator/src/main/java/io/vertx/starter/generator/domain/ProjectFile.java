package io.vertx.starter.generator.domain;

import io.vertx.starter.generator.domain.impl.ProjectFileImpl;

public interface ProjectFile {

  static ProjectFile file(String template, String destination) {
    return new ProjectFileImpl(template, destination);
  }

  String template();
  String destination();
}
