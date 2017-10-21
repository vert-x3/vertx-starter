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
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;

public class TemplateService {

    private final Logger log = LoggerFactory.getLogger(TemplateService.class);

    private Handlebars handlebars;

    public TemplateService(TemplateLoader loader) {
        this.handlebars = new Handlebars(loader);
    }

    public Future<String> render(String template, JsonObject object) {
        Context context = Context.newBuilder(object.getMap()).resolver(MapValueResolver.INSTANCE).build();
        log.debug("Rendering template {} with object {}", object);
        Future future = Future.future();
        try {
            future.complete(handlebars.compile(template).apply(context));
        } catch (IOException e) {
            log.error("Impossible to render template {}: ", template, e);
            future.fail(e.getCause());
        }
        return future;
    }

}
