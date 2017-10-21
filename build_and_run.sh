#!/usr/bin/env bash

java_opts="$JAVA_OPTS -Dvertx.logger-delegate-factory-class-name=\"io.vertx.core.logging.SLF4JLogDelegateFactory\""
jar_name="vertx-starter-main/target/vertx-starter-main-1.0.0-SNAPSHOT-fat.jar"
vertx_opts="-conf vertx-starter-main/conf/default-conf.json"

mvn clean package -DskipTests=true && \
mvn exec:exec -Dexec.executable="java" -Dexec.args="${java_opts} -jar ${jar_name} ${vertx_opts}"
