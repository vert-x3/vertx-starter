package io.vertx.starter

import io.vertx.core.AbstractVerticle
import io.vertx.core.Future

class MainVerticle : AbstractVerticle() {

  override fun start(startFuture: Future<Void>) {
    vertx
      .createHttpServer()
      .requestHandler { req ->
        req.response()
          .putHeader("content-type", "text/plain")
          .end("Hello from Vert.x!")
      }
      .listen(8080) { http ->
        if (http.succeeded()) {
          startFuture.complete()
          println("HTTP server started on port 8080")
        } else {
          startFuture.fail(http.cause());
        }
      }

  }

}
