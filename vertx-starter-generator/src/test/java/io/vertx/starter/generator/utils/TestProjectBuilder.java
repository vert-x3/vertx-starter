package io.vertx.starter.generator.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;

import static java.util.Arrays.asList;

public class TestProjectBuilder {

    private final String baseDir;
    private String language;
    private String buildTool;
    private String version;
    private String groupId;
    private String artifactId;
    private Set<String> dependencies = new HashSet<>();

    public TestProjectBuilder(String baseDir) {
        this.baseDir = baseDir;
    }

    public TestProjectBuilder language(String language) {
        this.language = language;
        return this;
    }

    public TestProjectBuilder java() {
        return language("java");
    }

    public TestProjectBuilder buildTool(String buildTool) {
        this.buildTool = buildTool;
        return this;
    }

    public TestProjectBuilder maven() {
        return buildTool("maven");
    }

    public TestProjectBuilder version(String version) {
        this.version = version;
        return this;
    }

    public TestProjectBuilder groupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public TestProjectBuilder artifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public TestProjectBuilder dependency(String  dependency) {
        this.dependencies.add(dependency);
        return this;
    }

    public TestProjectBuilder dependencies(String... dependencies) {
        this.dependencies.addAll(asList(dependencies));
        return this;
    }

    public JsonObject build() {
        return new JsonObject()
            .put("baseDir", baseDir)
            .put("language", language)
            .put("buildTool", buildTool)
            .put("version", version)
            .put("groupId", groupId)
            .put("artifactId", artifactId)
            .put("dependencies", new JsonArray(new ArrayList(dependencies)));
    }
}
