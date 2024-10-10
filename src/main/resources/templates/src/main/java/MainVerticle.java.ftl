package ${packageName};

<#if futurizedVerticle>
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
<#else>
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
</#if>

public class MainVerticle extends ${futurizedVerticle?then('VerticleBase', 'AbstractVerticle')} {

<#if futurizedVerticle>
  @Override
  public Future<?> start() {
    return vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8888).onSuccess(http -> {
      System.out.println("HTTP server started on port 8888");
    });
  }
<#else>
  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Hello from Vert.x!");
    }).listen(8888).onComplete(http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port 8888");
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
</#if>
}
