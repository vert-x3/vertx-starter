package io.vertx.starter.generator.service;

import io.vertx.core.json.JsonObject;

public class GradleBuildKotlinProject extends ProjectGenerator {

  public GradleBuildKotlinProject(TemplateService templateService, JsonObject project) {
    super(templateService, project);
  }

  @Override
  public void generate() {
    render("build.kotlin.gradle", "build.gradle");
    render("settings.gradle", "settings.gradle");
    copy("gradlew", "gradlew");
    copy("gradlew.bat", "gradlew.bat");
    copy("gradle/wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.jar");
    copy("gradle/wrapper/gradle-wrapper.properties", "gradle/wrapper/gradle-wrapper.properties");
  }

}

