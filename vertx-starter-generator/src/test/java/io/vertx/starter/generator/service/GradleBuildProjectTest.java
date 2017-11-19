package io.vertx.starter.generator.service;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class GradleBuildProjectTest extends BaseProjectTest {

  private JsonObject GRADLE_PROJECT = basicTestProject().gradle().java().build();

  @Test
  public void shouldGenerateMavenBuildProjectFiles(TestContext context) {
    final Async async = context.async();
    GradleBuildProject gradleBuildProject = new GradleBuildProject(templateService, GRADLE_PROJECT);

    gradleBuildProject.run(onTestDone -> {
      assertFileExists(context, "build.gradle");
      assertFileExists(context, "settings.gradle");
      assertFileExists(context, "gradlew");
      assertFileExists(context, "gradlew.bat");
      assertFileExists(context, "gradle/wrapper/gradle-wrapper.jar");
      assertFileExists(context, "gradle/wrapper/gradle-wrapper.properties");
      async.complete();
    });
  }

}
