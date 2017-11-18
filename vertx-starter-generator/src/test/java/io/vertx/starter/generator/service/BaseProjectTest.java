package io.vertx.starter.generator.service;

import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.starter.generator.utils.TestProjectBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public abstract class BaseProjectTest {

  protected static final String DEFAULT_GROUP_ID = "com.example";
  protected static final String DEFAULT_ARTIFACT_ID = "demo";
  protected static final String DEFAULT_VERSION = "1.0.0";

  protected static String TEST_ROOT_DIR = "target/test-vertx-starter/";
  protected static String TEST_BASE_DIR = "target/test-vertx-starter/" + DEFAULT_ARTIFACT_ID + "/";

  protected TemplateService templateService;

  protected Vertx vertx;

  public static TestProjectBuilder basicTestProject() {
    return new TestProjectBuilder(TEST_BASE_DIR)
      .groupId(DEFAULT_GROUP_ID)
      .artifactId(DEFAULT_ARTIFACT_ID)
      .version(DEFAULT_VERSION);
  }

  @Before
  public void beforeEach(TestContext context) {
    vertx = Vertx.vertx();
    templateService = new TemplateService(vertx, new FileTemplateLoader("src/main/resources/templates"));
  }

  @After
  public void afterEach(TestContext context) {
    vertx.fileSystem().deleteRecursiveBlocking(TEST_ROOT_DIR, true);
    vertx.close(context.asyncAssertSuccess());
  }

  public void assertFileExists(TestContext context, String prefix, String... others) {
    assertFileExists(context, prefix + "/" + String.join("/", others));
  }

  public void assertFileExists(TestContext context, String filename) {
    vertx.fileSystem().exists(TEST_BASE_DIR + filename, it -> {
      if (it.succeeded()) {
        context.assertTrue(it.result());
      } else {
        context.fail(it.cause());
      }
    });
  }
}
