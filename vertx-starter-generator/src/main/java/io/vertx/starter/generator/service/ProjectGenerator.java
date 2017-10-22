package io.vertx.starter.generator.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public abstract class ProjectGenerator {

    private final Logger log = LoggerFactory.getLogger(ProjectGenerator.class);

    private final TemplateService templateService;
    private final JsonObject project;

    private List<Future> futures = new ArrayList<>();

    public ProjectGenerator(TemplateService templateService, JsonObject project) {
        this.templateService = templateService;
        this.project = project;
    }

    protected String groupId() {
        return project.getString("groupId");
    }

    protected String artifactId() {
        return project.getString("artifactId");
    }

    protected String buildTool() {
        return project.getString("build");
    }

    protected String language() {
        return project.getString("language");
    }

    protected String baseDir() {
        return project.getString("baseDir");
    }

    public void render(String template, String destination) {
        futures.add(templateService.render(template, format("%s/%s", baseDir(), destination), project));
    }

    public void copy(String source, String destination) {
        futures.add(templateService.copy(source, format("%s/%s", baseDir(), destination)));
    }

    public abstract void generate();

    public void run(Handler<AsyncResult<List<String>>> handler) {
        generate();
        CompositeFuture.all(futures).setHandler(ar -> {
            if (ar.succeeded()) {
                handler.handle(Future.succeededFuture(ar.result().list()));
            } else {
                handler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }
}
