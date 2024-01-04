/*
 * Copyright 2023 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.starter;

import io.vertx.core.impl.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.ProcessBuilder.Redirect.DISCARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ScriptsTest {

  @TempDir
  Path analyticsDir;
  @TempDir
  Path testDir;
  Path workDir;
  Path dbFile;

  List<Runnable> cleanupTasks;

  @BeforeEach
  void setUp() {
    assumeThat(Utils.isWindows()).isFalse().withFailMessage("Windows not supported");
    workDir = testDir.resolve("work-dir");
    dbFile = testDir.resolve("vertx-starter.db");
    cleanupTasks = new ArrayList<>();
  }

  @AfterEach
  void tearDown() {
    cleanupTasks.forEach(Runnable::run);
  }

  @Test
  void failsIfNoArgs() throws Exception {
    assertThat(runScript(null)).isGreaterThan(0);
    assertThat(workDir).doesNotExist();
    assertThat(dbFile).doesNotExist();
  }

  @Test
  void failsIfAnalyticsDirIsNotADirectory() throws Exception {
    assertThat(runScript(List.of("pom.xml"))).isGreaterThan(0);
    assertThat(workDir).doesNotExist();
    assertThat(dbFile).doesNotExist();
  }

  @Test
  void doesNothingIfAnalyticsDirIsEmpty() throws Exception {
    assertThat(runScript(List.of(analyticsDir.toString()))).isEqualTo(0);
    assertThat(workDir).doesNotExist();
    assertThat(dbFile).doesNotExist();
  }

  @Test
  void createsDbFromFiles() throws Exception {
    copySamples();
    assertThat(runScript(List.of(analyticsDir.toString()))).isEqualTo(0);
    assertThat(workDir).doesNotExist();
    assertThat(dbFile).isRegularFile().isNotEmptyFile();
    executeQuery("SELECT count(*) as cnt FROM projects", rs -> {
      rs.next();
      assertEquals(10, rs.getInt("cnt"));
    });
    executeQuery("SELECT DISTINCT language FROM projects ORDER BY language", rs -> {
      rs.next();
      assertEquals("java", rs.getString("language"));
      rs.next();
      assertEquals("kotlin", rs.getString("language"));
    });
    executeQuery("SELECT DISTINCT value as dependency FROM projects, json_each(projects.vertx_dependencies) ORDER BY dependency ASC", rs -> {
      List<String> actualDependencies = new ArrayList<>();
      while (rs.next()) {
        actualDependencies.add(rs.getString("dependency"));
      }
      List<String> expectedDependencies = List.of(
        "vertx-auth-jwt",
        "vertx-lang-kotlin",
        "vertx-mysql-client",
        "vertx-pg-client",
        "vertx-redis-client",
        "vertx-web",
        "vertx-web-validation"
      );
      assertEquals(expectedDependencies, actualDependencies);
    });
  }

  @Test
  void failsIfWorkDirAlreadyExists() throws Exception {
    copySamples();
    assertTrue(workDir.toFile().mkdirs());
    assertThat(runScript(List.of(analyticsDir.toString()))).isGreaterThan(0);
    assertThat(dbFile).isRegularFile().isNotEmptyFile();
    executeQuery("SELECT count(*) as cnt FROM projects", rs -> {
      rs.next();
      assertEquals(0, rs.getInt("cnt"));
    });
  }


  @Test
  void updatesExistingDbFromFiles() throws Exception {
    copySamples();
    assertThat(runScript(List.of(analyticsDir.toString()))).isEqualTo(0);
    assertThat(workDir).doesNotExist();
    assertThat(dbFile).isRegularFile().isNotEmptyFile();
    copySamples();
    assertThat(runScript(List.of(analyticsDir.toString()))).isEqualTo(0);
    assertThat(workDir).doesNotExist();
    assertThat(dbFile).isRegularFile().isNotEmptyFile();
    executeQuery("SELECT count(*) as cnt FROM projects", rs -> {
      rs.next();
      assertEquals(20, rs.getInt("cnt"));
    });
  }

  private void executeQuery(String sql, ThrowingConsumer<ResultSet> consumer) throws Exception {
    try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath())) {
      try (PreparedStatement ps = connection.prepareStatement(sql)) {
        try (ResultSet t = ps.executeQuery()) {
          consumer.consume(t);
        }
      }
    }
  }

  private interface ThrowingConsumer<T> {
    void consume(T t) throws Exception;
  }

  private void copySamples() throws Exception {
    Path source = Paths.get("src/test/scripts/samples");
    Files.walkFileTree(source, Collections.emptySet(), 1, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.copy(file, analyticsDir.resolve(source.relativize(file)));
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private int runScript(List<String> arguments) throws Exception {
    ProcessBuilder processBuilder = new ProcessBuilder()
      .redirectOutput(DISCARD)
      .redirectError(DISCARD)
      .directory(testDir.toFile())
      .command("/bin/bash", Paths.get("src/main/scripts/update_db.sh").toAbsolutePath().toString());
    if (arguments != null) {
      processBuilder.command().addAll(arguments);
    }
    Process process = processBuilder.start();
    cleanupTasks.add(() -> process.destroyForcibly());
    return process.waitFor();
  }
}
