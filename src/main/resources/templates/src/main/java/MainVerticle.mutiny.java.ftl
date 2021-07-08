package ${packageName};

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

  @Override
  public Uni<Void> asyncStart() {
    return vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .endAndForget("Hello from Vert.x!");
    }).listen(8888)
      .onItem().invoke(() -> System.out.println("HTTP server started on port 8888"))
      .replaceWithVoid();
  }
}
