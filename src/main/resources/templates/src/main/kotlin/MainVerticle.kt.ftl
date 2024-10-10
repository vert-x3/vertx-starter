package ${packageName}

<#if futurizedVerticle>
import io.vertx.core.Future
import io.vertx.core.VerticleBase
<#else>
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
</#if>

class MainVerticle : ${futurizedVerticle?then('VerticleBase', 'AbstractVerticle')}() {

<#if futurizedVerticle>
  override fun start() : Future<*> {
    return vertx
      .createHttpServer()
      .requestHandler { req ->
        req.response()
          .putHeader("content-type", "text/plain")
          .end("Hello from Vert.x!")
      }
    .listen(8888).onSuccess { http ->
      println("HTTP server started on port 8888")
    }
  }
<#else>
  override fun start(startPromise: Promise<Void>) {
    vertx
      .createHttpServer()
      .requestHandler { req ->
        req.response()
          .putHeader("content-type", "text/plain")
          .end("Hello from Vert.x!")
      }
      .listen(8888).onComplete { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port 8888")
        } else {
          startPromise.fail(http.cause());
        }
      }
  }
</#if>
}
