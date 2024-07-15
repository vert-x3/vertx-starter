package ${packageName};

<#if hasVertxJUnit5>
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
<#elseif hasVertxUnit>
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
</#if>

<#if hasVertxJUnit5>
@ExtendWith(VertxExtension.class)
<#elseif hasVertxUnit>
@RunWith(VertxUnitRunner.class)
</#if>
public class TestMainVerticle {

<#if hasVertxJUnit5>
  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle()).onComplete(testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void verticle_deployed(Vertx vertx, VertxTestContext testContext) throws Throwable {
    testContext.completeNow();
  }
<#elseif hasVertxUnit>
  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  @Before
  public void deploy_verticle(TestContext testContext) {
    Vertx vertx = rule.vertx();
    vertx.deployVerticle(new MainVerticle()).onComplete(testContext.asyncAssertSuccess());
  }

  @Test
  public void verticle_deployed(TestContext testContext) throws Throwable {
    Async async = testContext.async();
    async.complete();
  }
</#if>
}
