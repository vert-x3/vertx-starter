package io.vertx.starter.generator.service;

import io.vertx.core.json.JsonObject;
import io.vertx.starter.generator.domain.ProjectFile;

import java.util.stream.Stream;

public class ProjectAndProjectFiles {

        public final JsonObject project;
        public final Stream<ProjectFile> projectFiles;

        public ProjectAndProjectFiles(JsonObject project, Stream<ProjectFile> projectFiles) {
            this.project = project;
            this.projectFiles = projectFiles;
        }
    }
