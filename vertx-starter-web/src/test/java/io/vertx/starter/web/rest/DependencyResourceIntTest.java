package io.vertx.starter.web.rest;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.codec.BodyCodec;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(VertxUnitRunner.class)
public class DependencyResourceIntTest extends AbstractResourceIntTest {

    @Test
    public void shouldFindAll(TestContext context) {
        Async async = context.async();
        webClient.get("/api/dependencies").as(BodyCodec.jsonArray()).send(response -> {
            if (response.succeeded()) {
                assertThat(response.result().statusCode(), is(200));
                JsonArray payload = response.result().body();
                assertThat(payload.size(), is(2));
                async.complete();
            } else {
                context.fail(response.cause());
                async.complete();
            }
        });
    }
}
