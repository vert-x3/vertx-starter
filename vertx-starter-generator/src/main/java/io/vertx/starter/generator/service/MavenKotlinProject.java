package io.vertx.starter.generator.service;

import io.vertx.core.json.JsonObject;

public class MavenKotlinProject extends BasicProject {

  public static final String SRC_MAIN_SOURCES_DIR = "src/main/kotlin/";
  public static final String SRC_MAIN_RESOURCES_DIR = "src/main/resources/";
  public static final String SRC_TEST_SOURCES_DIR = "src/test/kotlin/";
  public static final String SRC_TEST_RESOURCES_DIR = "src/test/resources/";

  private MavenBuildKotlinProject mavenBuildKotlinProject;

  public MavenKotlinProject(TemplateService templateService, JsonObject project) {
    super(templateService, project);
    mavenBuildKotlinProject = new MavenBuildKotlinProject(templateService, project);
  }

  private String packageDir() {
    return String.format("%s/%s/", SRC_MAIN_SOURCES_DIR, ProjectUtils.packageDir(groupId(), artifactId()));
  }

  @Override
  public void generate() {
    super.generate();
    mavenBuildKotlinProject.generate();
    render(SRC_MAIN_SOURCES_DIR + "package/MainVerticle.kt", packageDir() + "MainVerticle.kt");
  }
}
