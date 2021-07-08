package ${packageName}

import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.vertx.core.AbstractVerticle

class MainVerticle : AbstractVerticle() {

  override fun asyncStart(): Uni<Void> {
    return vertx.createHttpServer().requestHandler { req ->
      req.response()
        .putHeader("content-type", "text/plain")
        .endAndForget("Hello from Vert.x!")
    }.listen(8888)
      .onItem().invoke(Runnable { println("HTTP server started on port 8888") })
      .replaceWithVoid()
  }
}
