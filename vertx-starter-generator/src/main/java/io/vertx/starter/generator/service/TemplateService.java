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

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.io.TemplateLoader;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;

import static java.util.Objects.requireNonNull;

public class TemplateService {

    private final Logger log = LoggerFactory.getLogger(TemplateService.class);

    private Vertx vertx;
    private TemplateLoader loader;
    private Handlebars handlebars;

    public TemplateService(Vertx vertx, TemplateLoader loader) {
        this.vertx = vertx;
        this.loader = loader;
        this.handlebars = new Handlebars(loader);
    }

    public Future<String> render(String template, String destination, JsonObject object) {
        Context context = Context.newBuilder(object.getMap()).resolver(MapValueResolver.INSTANCE).build();
        log.debug("Rendering template {} with object {}", object);
        Future future = Future.future();
        try {
            future.complete(writeFile(destination, handlebars.compile(template).apply(context)));
        } catch (IOException e) {
            log.error("Impossible to render template {}: ", template, e);
            future.fail(e.getCause());
        }
        return future;
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
                log.debug("File {} written", filename);
                future.complete(filename);
            }
        });
        return future;
    }

    private Path getPathInJar(String filename) throws IOException {
        URL url = getClass().getResource(filename);
        String jarPath = url.toString().split("!")[0];
        try (FileSystem jarfs = FileSystems.newFileSystem(URI.create(jarPath), new HashMap<>());) {
            log.debug("Getting file from jar: {}", filename);
            return jarfs.getPath(filename);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw e;
        }
    }

    private Future<Path> getPath(String filename) {
        Future future = Future.future();
        URL url = getClass().getResource(filename);
        if (url.getProtocol().startsWith("jar")) {
            try {
                Path path = getPathInJar(filename);
                future.complete(path);
            } catch (IOException e) {
                e.printStackTrace();
                future.fail(e);
            }
        } else {
            future.complete(Paths.get(filename));
        }
        return future;
    }

    public Future<String> mkdirs(String path) {
        Future future = Future.future();
        Path p = Paths.get(path);
        if (!p.toFile().isDirectory()) {
            p = p.getParent();
        }
        String dir = p.toString();
        vertx.fileSystem().exists(dir, onExistenceTested -> {
            if (onExistenceTested.succeeded()) {
                if (!onExistenceTested.result()) {
                    vertx.fileSystem().mkdirs(dir, onDirectoryCreated -> {
                        if (onDirectoryCreated.succeeded()) {
                            future.complete(dir);
                        } else {
                            future.fail(onDirectoryCreated.cause());
                        }
                    });
                } else {
                    log.debug("Directory {} already exists", dir);
                    future.complete(dir);
                }
            } else {
                future.fail(onExistenceTested.cause());
            }
        });
        return future;
    }

    private Future<String> copyFileFromJar(String jarPath, String filename, String destination) {
        log.debug("Copying {} from jar {} to {}", filename, jarPath, destination);
        Future future = Future.future();
        try (FileSystem jarfs = FileSystems.newFileSystem(URI.create(jarPath), new HashMap<>())) {
            Files.copy(jarfs.getPath(filename), Paths.get(destination));
            future.complete(filename);
        } catch (IOException e) {
            log.error("Impossible to copy file {} from jar {} to {}: {}", filename, jarPath, destination, e.getMessage());
            future.fail(e);
        }
        return future;
    }

    private Future<String> copyRegularFile(String filename, String destination) {
        log.debug("Copying {} to {}", Paths.get(filename), destination);
        Future future = Future.future();
        try {
            Files.copy(Paths.get(filename), Paths.get(destination));
            future.complete(filename);
        } catch (IOException e) {
            log.error("Impossible to copy file {} to {}: {}", filename, destination, e);
            future.fail(e);
        }
        return future;
    }

    private Future<String> copyInternal(String source, String destination) {
        URL url = getClass().getResource(source);
        if (url != null && url.getProtocol().startsWith("jar")) {
            String jarPath = url.toString().split("!")[0];
            return copyFileFromJar(jarPath, source, destination);
        }
        return copyRegularFile(source, destination);
    }

    public Future<String> copy(String source, String destination) {
        requireNonNull(source);
        requireNonNull(destination);
        log.debug("Copying {} to {}", source, destination);
        return mkdirs(destination)
            .compose((noop) -> copyInternal(loader.getPrefix() + source, destination));
    }

}
