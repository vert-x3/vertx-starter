package io.vertx.starter.web.rest;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(VertxUnitRunner.class)
public class ProjectResourceIntTest extends AbstractResourceIntTest {

    @Test
    public void shouldUseDefaultValuesWhenAParameterIsNotSet(TestContext context) {
        Async async = context.async();
        vertx.eventBus().consumer("project.requested").handler(message -> {
            message.reply(new JsonObject().put("archivePath", "src/test/resources/starter.zip"));
        });

        webClient.get("/api/starter.zip").sendJson(new JsonObject(), response -> {
            if (response.succeeded()) {
                assertThat(response.result().statusCode(), is(200));
                async.complete();
            } else {
                context.fail(response.cause());
                async.complete();
            }
        });
    }
}
