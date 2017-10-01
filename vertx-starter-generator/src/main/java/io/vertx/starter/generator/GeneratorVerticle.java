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
package io.vertx.starter.generator;

import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.starter.generator.service.ArchiveService;
import io.vertx.starter.generator.service.StarterService;
import io.vertx.starter.generator.service.ProjectGeneratorService;
import io.vertx.starter.generator.service.TemplateService;

public class GeneratorVerticle extends AbstractVerticle {

    private final Logger log = LoggerFactory.getLogger(GeneratorVerticle.class);

    public static final String TEMPLATE_DIR = "/templates";

    String tempDir() {
        return config().getString("temp.dir", System.getProperty("java.io.tmpdir"));
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        TemplateLoader loader = new ClassPathTemplateLoader(TEMPLATE_DIR);
        StarterService starter = new StarterService(vertx, tempDir());
        ProjectGeneratorService generator = new ProjectGeneratorService(vertx, new TemplateService(loader));
        ArchiveService archive = new ArchiveService(vertx);

        vertx.eventBus().<JsonObject>consumer("starter.starter").handler(starter::starter);
        vertx.eventBus().<JsonObject>consumer("starter.clean").handler(starter::clean);
        vertx.eventBus().<JsonObject>consumer("generate").handler(generator::generate);
        vertx.eventBus().<JsonObject>consumer("archive").handler(archive::archive);

        log.info("\n----------------------------------------------------------\n\t" +
                "{} is running!\n" +
                "----------------------------------------------------------",
            GeneratorVerticle.class.getSimpleName());
        startFuture.complete();
    }
}
