package io.vertx.starter.generator.domain.impl;

import io.vertx.starter.generator.domain.ProjectFile;

public class ProjectFileImpl implements ProjectFile{

  private final String template;
  private final String destination;

  public ProjectFileImpl(String template, String destination) {
    this.template = template;
    this.destination = destination;
  }

  @Override
  public String template() {
    return template;
  }

  @Override
  public String destination() {
    return destination;
  }
}
