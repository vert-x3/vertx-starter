package ${packageName}

import io.vertx.core.http.HttpMethod

import io.vertx.lang.scala.ImplicitConversions.vertxFutureToScalaFuture
import io.vertx.lang.scala.testing.VerticleTesting

import org.scalatest.matchers.should.Matchers

import scala.language.implicitConversions


class TestMainVerticle extends VerticleTesting[HttpVerticle], Matchers:

  "HttpVerticle" should "bind to 8888 and answer with 'Hello from Vert.x!'" in {
    val httpClient = vertx.createHttpClient()

    for {
      req  <- httpClient.request(HttpMethod.GET, 8888, "127.0.0.1", "/")
      res  <- req.send()
      body <- res.body.map(_.toString)
      assertion = body should equal("Hello from Vert.x!")
    } yield assertion
  }