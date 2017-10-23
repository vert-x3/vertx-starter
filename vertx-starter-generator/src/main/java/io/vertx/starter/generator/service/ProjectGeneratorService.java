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

public class ProjectGeneratorService {

    private final Logger log = LoggerFactory.getLogger(ProjectGeneratorService.class);

    private Vertx vertx;
    private TemplateService templateService;

    public ProjectGeneratorService(Vertx vertx, TemplateService templateService) {
        this.vertx = vertx;
        this.templateService = templateService;
    }

    public void generate(Message<JsonObject> message) {
        getGenerator(message.body()).run(onGenerationDone -> {
            if (onGenerationDone.failed()) {
                message.fail(500, onGenerationDone.cause().getMessage());
            } else {
                message.reply(null);
            }
        });
    }

    private ProjectGenerator getGenerator(JsonObject project) {
        String build = project.getString("build");
        String language = project.getString("language");
        ProjectGenerator projectGenerator = null;
        if (build.equalsIgnoreCase("maven") && language.equalsIgnoreCase("java")) {
            projectGenerator = new MavenJavaProject(templateService, project);
        }
        if (build.equalsIgnoreCase("gradle") && language.equalsIgnoreCase("java")) {
            projectGenerator = new GradleJavaProject(templateService, project);
        }
        return projectGenerator;
    }

}
