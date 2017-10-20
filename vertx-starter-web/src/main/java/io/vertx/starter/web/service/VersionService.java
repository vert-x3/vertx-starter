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
package io.vertx.starter.web.service;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class VersionService {

    public static final JsonArray VERSIONS = new JsonArray()
        .add("3.5.0")
        .add("3.4.2")
        .add("3.4.1")
        .add("3.4.0");

    public void findAll(Message<JsonObject> message) {
        JsonObject query = message.body();
        message.reply(VERSIONS);
    }
}
