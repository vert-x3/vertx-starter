package io.vertx.starter.generator.service;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.vertx.starter.generator.service.MavenJavaProject.SRC_MAIN_SOURCES_DIR;
import static io.vertx.starter.generator.service.ProjectUtils.packageDir;

@RunWith(VertxUnitRunner.class)
public class MavenJavaProjectTest extends BaseProjectTest {

  private JsonObject MAVEN_PROJECT = basicTestProject().maven().java().build();

  private String javaDir() {
    return SRC_MAIN_SOURCES_DIR + packageDir(DEFAULT_GROUP_ID, DEFAULT_ARTIFACT_ID);
  }

  @Test
  public void shouldGenerateMavenJavaProjectFiles(TestContext context) {
    final Async async = context.async();
    MavenJavaProject mavenJavaProject = new MavenJavaProject(templateService, MAVEN_PROJECT);

    mavenJavaProject.run(onTestDone -> {
      assertFileExists(context, javaDir(), "MainVerticle.java");
      async.complete();
    });
  }

}
