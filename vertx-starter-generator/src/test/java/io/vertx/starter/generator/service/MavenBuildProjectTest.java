package io.vertx.starter.generator.service;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MavenBuildProjectTest extends BaseProjectTest {

  private JsonObject MAVEN_PROJECT = basicTestProject().maven().java().build();

  @Test
  public void shouldGenerateMavenBuildProjectFiles(TestContext context) {
    final Async async = context.async();
    MavenBuildProject mavenBuildProject = new MavenBuildProject(templateService, MAVEN_PROJECT);

    mavenBuildProject.run(onTestDone -> {
      assertFileExists(context, "pom.xml");
      assertFileExists(context, "mvnw");
      assertFileExists(context, "mvnw.bat");
      assertFileExists(context, "maven/wrapper/maven-wrapper.jar");
      assertFileExists(context, "maven/wrapper/maven-wrapper.properties");
      async.complete();
    });
  }

}
