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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ProjectGeneratorService {

    private final Logger log = LoggerFactory.getLogger(ProjectGeneratorService.class);

    private static final Map<String, String> BUILD;
    private static final Map<String, String> LANGUAGES;

    static {
        Map<String, String> tmpBuild = new HashMap<>();
        tmpBuild.put("maven", "pom.xml");
        tmpBuild.put("gradle", "build.gradle");
        BUILD = Collections.unmodifiableMap(tmpBuild);
    }

    static {
        Map<String, String> tmpLanguages = new HashMap<>();
        tmpLanguages.put("java", "src/main/java/MainVerticle.java");
        tmpLanguages.put("gradle", "src/main/groovy/MainVerticle.groovy");
        LANGUAGES = Collections.unmodifiableMap(tmpLanguages);
    }


    private Vertx vertx;
    private TemplateService templateService;

    public ProjectGeneratorService(Vertx vertx, TemplateService templateService) {
        this.vertx = vertx;
        this.templateService = templateService;
    }

    public void generate(Message<JsonObject> message) {
        JsonObject metadata = message.body();
        String build = metadata.getString("build", "maven");
        String language = metadata.getString("language", "java");
        //Act as a activation flags in .gitignore
        metadata.put(build, true);
        metadata.put(language, true);
        String baseDir = metadata.getString("baseDir");
        CompositeFuture.all(
            generateFile(metadata, baseDir, BUILD.get(build)),
            generateFile(metadata, baseDir, LANGUAGES.get(language)),
            generateFile(metadata, baseDir, ".gitignore"),
            generateFile(metadata, baseDir, ".editorconfig")
        ).setHandler(ar -> {
            if (ar.failed()) {
                log.error("Impossible to generate project {} : {}", metadata, ar.cause().getMessage());
                message.fail(500, ar.cause().getMessage());
            } else {
                message.reply(null);
            }
        });
    }

    private Future<Void> generateFile(JsonObject metadata, String baseDir, String filename) {
        return templateService
            .render(filename, metadata)
            .compose(content -> writeFile(baseDir, filename, content));
    }

    private Future<Void> writeFile(String baseDir, String filename, String content) {
        requireNonNull(baseDir);
        requireNonNull(filename);
        requireNonNull(content);
        Future future = Future.future();
        String path = baseDir + "/" + filename;
        String parentDir = new File(path).getParentFile().getAbsolutePath();
        vertx.fileSystem().mkdirs(parentDir, onParentDirCreated -> {
            if (onParentDirCreated.failed()) {
                log.error("Impossible to create parent directory {} : {}", parentDir, onParentDirCreated.cause().getMessage());
                future.fail(onParentDirCreated.cause());
            } else {
                vertx.fileSystem().writeFile(path, Buffer.buffer(content), onFileWritten -> {
                    if (onFileWritten.failed()) {
                        log.error("Impossible to write file {} : {}", path, onFileWritten.cause().getMessage());
                        future.fail(onFileWritten.cause());
                    } else {
                        log.info("File {} written", path);
                        future.complete();
                    }
                });
            }
        });
        return future;
    }
}
