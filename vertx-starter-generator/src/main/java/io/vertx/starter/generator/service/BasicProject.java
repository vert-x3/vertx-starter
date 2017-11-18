package io.vertx.starter.generator.service;

import io.vertx.core.json.JsonObject;

public class BasicProject extends ProjectGenerator {

  public BasicProject(TemplateService templateService, JsonObject project) {
    super(templateService, project);
  }

  @Override
  public void generate() {
    render("gitignore", ".gitignore");
    copy("editorconfig", ".editorconfig");
  }

}
