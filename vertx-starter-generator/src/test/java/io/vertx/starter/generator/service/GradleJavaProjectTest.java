package io.vertx.starter.generator.service;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import static io.vertx.starter.generator.service.MavenJavaProject.SRC_MAIN_SOURCES_DIR;
import static io.vertx.starter.generator.service.ProjectUtils.packageDir;

public class GradleJavaProjectTest extends BaseProjectTest {

    private JsonObject GRADLE_PROJECT = basicTestProject().gradle().java().build();

    private String javaDir() {
        return SRC_MAIN_SOURCES_DIR + packageDir(DEFAULT_GROUP_ID, DEFAULT_ARTIFACT_ID);
    }

    @Test
    public void shouldGenerateGradleJavaProjectFiles(TestContext context) {
        final Async async = context.async();
        GradleJavaProject gradleJavaProject = new GradleJavaProject(templateService, GRADLE_PROJECT);

        gradleJavaProject.run(onTestDone -> {
            assertFileExists(context, javaDir(), "MainVerticle.java");
            async.complete();
        });
    }

}

