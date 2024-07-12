package ${packageName}

import io.vertx.core.{AbstractVerticle, Promise}
import io.vertx.lang.scala.*
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.ImplicitConversions.vertxFutureToScalaFuture

import scala.concurrent.Future
import scala.language.implicitConversions

class MainVerticle extends AbstractVerticle:

  override def start(startPromise: Promise[Void]): Unit =
    vertx
      .deployVerticle(HttpVerticle())
      .onSuccess(_ => startPromise.complete)
      .onFailure(e => startPromise.fail(e))


class HttpVerticle extends ScalaVerticle:

  override def asyncStart: Future[Unit] =
    vertx
      .createHttpServer()
      .requestHandler(_.response
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!"))
      .listen(8888)
      .mapEmpty[Unit]()
