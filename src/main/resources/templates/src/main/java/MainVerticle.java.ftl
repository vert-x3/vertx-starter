package ${packageName};

import io.vertx.core.AbstractVerticle;
<#if vertxVersion?starts_with("3") && !vertxVersion?starts_with("3.8") && !vertxVersion?starts_with("3.9")>
import io.vertx.core.Future;
<#else>
import io.vertx.core.Promise;
</#if>

public class MainVerticle extends AbstractVerticle {

  @Override
  <#if vertxVersion?starts_with("3") && !vertxVersion?starts_with("3.8") && !vertxVersion?starts_with("3.9")>
  public void start(Future<Void> startFuture) throws Exception {
  <#else>
  public void start(Promise<Void> startPromise) throws Exception {
  </#if>
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8888, http -> {
      if (http.succeeded()) {
    <#if vertxVersion?starts_with("3") && !vertxVersion?starts_with("3.8") && !vertxVersion?starts_with("3.9")>
        startFuture.complete();
    <#else>
        startPromise.complete();
    </#if>
        System.out.println("HTTP server started on port 8888");
      } else {
    <#if vertxVersion?starts_with("3") && !vertxVersion?starts_with("3.8") && !vertxVersion?starts_with("3.9")>
        startFuture.fail(http.cause());
    <#else>
        startPromise.fail(http.cause());
    </#if>
      }
    });
  }
}
