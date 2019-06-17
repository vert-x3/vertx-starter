package ${packageName}

import io.vertx.core.AbstractVerticle
<#if vertxVersion?index_of("3") != 0>
import io.vertx.core.Promise
<#else>
import io.vertx.core.Future
</#if>

class MainVerticle : AbstractVerticle() {

  <#if vertxVersion?index_of("3") != 0>
  override fun start(startFuture: Primise<Void>) {
  <#else>
  override fun start(startFuture: Future<Void>) {
  </#if>
    vertx
      .createHttpServer()
      .requestHandler { req ->
        req.response()
          .putHeader("content-type", "text/plain")
          .end("Hello from Vert.x!")
      }
      .listen(8888) { http ->
        if (http.succeeded()) {
          startFuture.complete()
          println("HTTP server started on port 8888")
        } else {
          startFuture.fail(http.cause());
        }
      }
  }
}
