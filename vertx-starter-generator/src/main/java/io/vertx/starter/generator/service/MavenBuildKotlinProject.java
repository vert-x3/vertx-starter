package io.vertx.starter.generator.service;

import io.vertx.core.json.JsonObject;


public class MavenBuildKotlinProject extends ProjectGenerator {

  public MavenBuildKotlinProject(TemplateService templateService, JsonObject project) {
    super(templateService, project);
  }

  @Override
  public void generate() {
    render("pom.kotlin.xml", "pom.xml");
    copy("mvnw", "mvnw");
    copy("mvnw.bat", "mvnw.bat");
    copy("maven/wrapper/maven-wrapper.jar", "maven/wrapper/maven-wrapper.jar");
    copy("maven/wrapper/maven-wrapper.properties", "maven/wrapper/maven-wrapper.properties");
  }

}
