package ${packageName}

<#if hasVertxJUnit5>
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
<#elseif hasVertxUnit>
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.RunTestOnContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
</#if>

<#if hasVertxJUnit5>
@ExtendWith(VertxExtension::class)
<#elseif hasVertxUnit>
@RunWith(VertxUnitRunner::class)
</#if>
class TestMainVerticle {

<#if hasVertxJUnit5>
  @BeforeEach
  fun deploy_verticle(vertx: Vertx, testContext: VertxTestContext) {
    vertx.deployVerticle(MainVerticle(), testContext.succeeding<String> { _ -> testContext.completeNow() })
  }

  @Test
  fun verticle_deployed(vertx: Vertx, testContext: VertxTestContext) {
    testContext.completeNow()
  }
<#elseif hasVertxUnit>
  @Rule
  @JvmField
  val rule = RunTestOnContext()

  @Before
  fun deploy_verticle(testContext: TestContext) {
    val vertx = rule.vertx()
    vertx.deployVerticle(MainVerticle(), testContext.asyncAssertSuccess())
  }

  @Test
  fun verticle_deployed(testContext: TestContext) {
    val async = testContext.async()
    async.complete()
  }
</#if>
}
