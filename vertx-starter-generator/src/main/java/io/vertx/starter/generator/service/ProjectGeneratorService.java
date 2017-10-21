/*
 * Copyright (c) 2017 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.starter.generator.service;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.starter.generator.domain.ProjectFile;
import io.vertx.starter.generator.domain.ProjectFilesProvider;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class ProjectGeneratorService {

    private final Logger log = LoggerFactory.getLogger(ProjectGeneratorService.class);

    private Vertx vertx;
    private TemplateService templateService;

    public ProjectGeneratorService(Vertx vertx, TemplateService templateService) {
        this.vertx = vertx;
        this.templateService = templateService;
    }

    public void generate(Message<JsonObject> message) {
        generate(message.body()).setHandler(onGenerationDone -> {
            if (onGenerationDone.failed()) {
                message.fail(500, onGenerationDone.cause().getMessage());
            } else {
                message.reply(null);
            }
        });
    }

    public Future<Void> generate(JsonObject project) {
        Future future = Future.future();
        String groupId = project.getString("groupId", "com.example");
        String artifactId = project.getString("artifactId", "demo");
        String build = project.getString("build", "maven");
        String language = project.getString("language", "java");
        String baseDir = project.getString("baseDir");

        //Act as a activation flags in .gitignore
        project.put(build, true);
        project.put(language, true);

        CompositeFuture.all(
            ProjectFilesProvider.projectFiles(groupId, artifactId, language, build)
                .map(projectFile -> renderAndWrite(baseDir, projectFile, project))
                .collect(Collectors.toList())
        ).setHandler(ar -> {

            if (ar.failed()) {
                log.error("Impossible to generate project {} : {}", project, ar.cause().getMessage());

                future.fail(ar.cause().getMessage());
            } else {
                future.complete();
            }
        });
        return future;
    }

    private Future<String> renderAndWrite(String baseDir, ProjectFile projectFile, JsonObject values) {
        return templateService
            .render(projectFile.template(), values)
            .compose(content -> writeFile(baseDir + "/" + projectFile.destination(), content));
    }

    private Future<String> writeFile(String filename, String content) {
        requireNonNull(filename);
        requireNonNull(content);
        Future future = Future.future();
        String parent = Paths.get(filename).getParent().toAbsolutePath().toString();
        boolean exists = vertx.fileSystem().existsBlocking(parent);
        if (!exists) {
            vertx.fileSystem().mkdirsBlocking(parent);
        }
        vertx.fileSystem().writeFile(filename, Buffer.buffer(content), onFileWritten -> {
            if (onFileWritten.failed()) {
                log.error("Impossible to write file {} : {}", filename, onFileWritten.cause().getMessage());
                future.fail(onFileWritten.cause());
            } else {
                log.info("File {} written", filename);
                future.complete(filename);
            }
        });
        return future;
    }

}
