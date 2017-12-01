package io.vertx.starter.web.rest;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.starter.web.WebVerticle;
import org.junit.After;
import org.junit.Before;

public class AbstractResourceIntTest {

    protected Vertx vertx;
    protected WebClient webClient;

    @Before
    public void beforeEach(TestContext context) {
        vertx = Vertx.vertx();
        int httpPort = 8000;
        JsonObject config = new JsonObject()
            .put("dependencies.path", "src/test/resources/dependencies.json")
            .put("project.request", new JsonObject())
            .put("http.port", httpPort);
        vertx.deployVerticle(new WebVerticle(), new DeploymentOptions().setConfig(config), context.asyncAssertSuccess());
        webClient = WebClient.create(vertx, new WebClientOptions().setDefaultPort(httpPort));
    }

    @After
    public void afterEach(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
}
