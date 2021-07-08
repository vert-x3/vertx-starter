package ${packageName}

import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.mutiny.core.Vertx
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class TestMainVerticle {

  @BeforeEach
  fun deploy_verticle(vertx: Vertx, testContext: VertxTestContext) {
    vertx.deployVerticle(MainVerticle())
      .subscribe().with { testContext.completeNow() }
  }

  @Test
  fun verticle_deployed(vertx: Vertx, testContext: VertxTestContext) {
    testContext.completeNow()
  }
}
