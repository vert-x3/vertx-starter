package ${packageName}

import io.vertx.core.AbstractVerticle
<#if vertxVersion?starts_with("3")>
import io.vertx.core.Future
<#else>
import io.vertx.core.Promise
</#if>

class MainVerticle : AbstractVerticle() {

  <#if vertxVersion?starts_with("3")>
  override fun start(startFuture: Future<Void>) {
  <#else>
  override fun start(startPromise: Promise<Void>) {
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
      <#if vertxVersion?starts_with("3")>
          startFuture.complete()
      <#else>
          startPromise.complete()
      </#if>
          println("HTTP server started on port 8888")
        } else {
      <#if vertxVersion?starts_with("3")>
          startFuture.fail(http.cause());
      <#else>
          startPromise.fail(http.cause());
      </#if>
        }
      }
  }
}
