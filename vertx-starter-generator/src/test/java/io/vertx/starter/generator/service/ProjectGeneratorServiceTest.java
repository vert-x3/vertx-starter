package io.vertx.starter.generator.service;

import com.github.jknack.handlebars.io.FileTemplateLoader;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
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

import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(VertxUnitRunner.class)
public class ProjectGeneratorServiceTest {

    private static final String DEFAULT_GROUP_ID = "com.example";
    private static final String DEFAULT_ARTIFACT_ID = "demo";
    private static final String DEFAULT_VERSION = "1.0.0";

    private static final ProjectFiles BASIC_PROJECT_FILES = new BasicProjectFiles();

    private static String TEST_ROOT_DIR = "target/test-vertx-starter/";
    private static String TEST_BASE_DIR = "target/test-vertx-starter/" + DEFAULT_ARTIFACT_ID + "/";

    private TemplateService templateService;
    private ProjectGeneratorService projectGeneratorService;

    private Vertx vertx;

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

    private TestProjectBuilder basicTestProject() {
        return new TestProjectBuilder(TEST_BASE_DIR)
            .groupId(DEFAULT_GROUP_ID)
            .artifactId(DEFAULT_ARTIFACT_ID)
            .version(DEFAULT_VERSION);
    }

    public Future<Void> generateAndTest(TestContext context, JsonObject project, Stream<ProjectFile> projectFiles) {
        return projectGeneratorService.generate(project).setHandler(ar -> {
            assertProjectFilesExists(context, projectFiles);
        });
    }

    public Future assertProjectFilesExists(TestContext context, Stream<ProjectFile> projectFiles) {
        return CompositeFuture.all(
            projectFiles
                .map(projectFile -> assertProjectFileExists(context, TEST_BASE_DIR + "/" + projectFile.destination()))
                .collect(Collectors.toList()));
    }

    private Future<Void> assertProjectFileExists(TestContext context, String filename) {
        Future future = Future.future();
        vertx.fileSystem().exists(filename, it -> {
            context.assertTrue(it.result());
            future.complete();
        });
        return future;
    }

    @Test
    public void shouldGenerateMavenJavaProjectFiles(TestContext context) {
        final Async async = context.async();
        JsonObject project = basicTestProject().java().maven().build();
        Stream<ProjectFile> projectFiles = Stream.concat(
            BASIC_PROJECT_FILES.files(),
            new MavenJavaProjectFiles(DEFAULT_GROUP_ID, DEFAULT_ARTIFACT_ID).files()
        );

        generateAndTest(context, project, projectFiles).setHandler(onTestDone -> {
            async.complete();
        });
    }

}
