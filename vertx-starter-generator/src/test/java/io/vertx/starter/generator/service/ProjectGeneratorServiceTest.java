package io.vertx.starter.generator.service;

import com.github.jknack.handlebars.io.FileTemplateLoader;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.starter.generator.domain.ProjectFile;
import io.vertx.starter.generator.domain.ProjectFiles;
import io.vertx.starter.generator.domain.impl.BasicProjectFiles;
import io.vertx.starter.generator.domain.impl.MavenJavaProjectFiles;
import io.vertx.starter.generator.utils.TestProjectBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.vertx.starter.generator.service.Projects.mavenJava;

@RunWith(VertxUnitRunner.class)
public class ProjectGeneratorServiceTest {

    public static final String DEFAULT_GROUP_ID = "com.example";
    public static final String DEFAULT_ARTIFACT_ID = "demo";
    public static final String DEFAULT_VERSION = "1.0.0";

    public static String TEST_ROOT_DIR = "target/test-vertx-starter/";
    public static String TEST_BASE_DIR = "target/test-vertx-starter/" + DEFAULT_ARTIFACT_ID + "/";

    private TemplateService templateService;
    private ProjectGeneratorService projectGeneratorService;

    private Vertx vertx;

    private final List<ProjectAndProjectFiles> projectAndProjectFiles = new ArrayList<>();

    public ProjectGeneratorServiceTest() {
        projectAndProjectFiles.add(mavenJava(TEST_BASE_DIR, DEFAULT_GROUP_ID, DEFAULT_ARTIFACT_ID, DEFAULT_VERSION));
    }

    @Before
    public void beforeEach(TestContext context) {
        vertx = Vertx.vertx();
        templateService = new TemplateService(new FileTemplateLoader("src/main/resources/templates", ".hbs"));
        projectGeneratorService = new ProjectGeneratorService(vertx, templateService);
    }

    @After
    public void afterEach(TestContext context) {
        vertx.fileSystem().deleteRecursive(TEST_ROOT_DIR, true, context.asyncAssertSuccess());
        vertx.close(context.asyncAssertSuccess());
    }

    public void assertProjectFileExists(TestContext context, String filename) {
        vertx.fileSystem().exists(filename, it -> {
            context.assertTrue(it.result());
        });
    }

    private void generateAndTest(TestContext context, JsonObject project, Stream<ProjectFile> projectFiles) {
        final Async async = context.async();
        projectGeneratorService
            .generate(project)
            .setHandler(onTestDone -> {
                projectFiles.forEach(projectFile -> assertProjectFileExists(context, TEST_BASE_DIR + "/" + projectFile.destination()));
                async.complete();
            });
    }

    @Test
    public void shouldGenerateMavenJavaProjectFiles(TestContext context) {
        projectAndProjectFiles.stream().forEach(it -> generateAndTest(context, it.project, it.projectFiles));
    }

}
