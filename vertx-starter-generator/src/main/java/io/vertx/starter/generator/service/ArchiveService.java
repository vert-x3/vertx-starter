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

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;

public class ArchiveService {

    private final Logger log = LoggerFactory.getLogger(ArchiveService.class);

    private Vertx vertx;

    public ArchiveService(Vertx vertx) {
        this.vertx = vertx;
    }

    public void archive(Message<JsonObject> message) {
        JsonObject metadata = message.body();
        String baseDir = metadata.getString("baseDir");
        String rootDir = metadata.getString("rootDir");
        String archive = rootDir + "/archive.zip";
        vertx.fileSystem().createFile(archive, ar -> {
            if (ar.failed()) {
                log.error("Impossible to create file {}: {}", archive, ar.cause().getMessage());
                message.fail(500, ar.cause().getMessage());
            } else {
                ZipUtil.pack(new File(baseDir), new File(archive), true);
                message.reply(archive);
            }
        });
    }
}
