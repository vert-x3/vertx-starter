package ${packageName};

import io.vertx.core.AbstractVerticle;
<#if vertxVersion?index_of("3") != 0>
import io.vertx.core.Promise;
<#else>
import io.vertx.core.Future;
</#if>

public class MainVerticle extends AbstractVerticle {

  @Override
  <#if vertxVersion?index_of("3") != 0>
  public void start(Promise<Void> startFuture) throws Exception {
  <#else>
  public void start(Future<Void> startFuture) throws Exception {
  </#if>
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8888, http -> {
      if (http.succeeded()) {
        startFuture.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startFuture.fail(http.cause());
      }
    });
  }
}
