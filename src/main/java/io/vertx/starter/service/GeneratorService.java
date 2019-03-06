/*
 * Copyright (c) 2017-2018 Daniel Petisme
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.starter.service;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import io.vertx.starter.model.ArchiveFormat;
import io.vertx.starter.model.BuildTool;
import io.vertx.starter.model.Language;
import io.vertx.starter.model.VertxProject;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GeneratorService {

  private final Logger log = LoggerFactory.getLogger(GeneratorService.class);

  private final Vertx vertx;
  private final FreeMarkerTemplateEngine templateEngine;


  public GeneratorService(Vertx vertx) {
    this.vertx = vertx;
    templateEngine = FreeMarkerTemplateEngine.create(vertx);
  }

  public Buffer onProjectRequested(Message<JsonObject> message) throws Exception {
    VertxProject project = message.body().mapTo(VertxProject.class);

    ArchiveOutputStreamFactory factory;
    ArchiveFormat archiveFormat = project.getArchiveFormat();
    if (archiveFormat == ArchiveFormat.TGZ) {
      factory = baos -> new TarArchiveOutputStream(new GzipCompressorOutputStream(baos));
    } else if (archiveFormat == ArchiveFormat.ZIP) {
      factory = baos -> new ZipArchiveOutputStream(baos);
    } else {
      throw new IllegalArgumentException("Unsupported archive format: " + archiveFormat.getFileExtension());
    }

    try (TempDir tempDir = TempDir.create();
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ArchiveOutputStream out = factory.create(baos)) {

      createProject(project, tempDir);
      generateArchive(tempDir, out);

      out.finish();
      out.close();

      return Buffer.buffer(baos.toByteArray());
    }
  }

  private void createProject(VertxProject project, TempDir tempDir) throws IOException {
    log.debug("Generating project: {}", project);

    Map<String, Object> ctx = new HashMap<>();
    ctx.put("buildTool", project.getBuildTool().name().toLowerCase());
    ctx.put("groupId", project.getGroupId());
    ctx.put("artifactId", project.getArtifactId());
    ctx.put("language", project.getLanguage());
    ctx.put("vertxVersion", project.getVertxVersion());
    Set<String> vertxDependencies = project.getVertxDependencies();
    vertxDependencies.addAll(project.getLanguage().getLanguageDependencies());
    ctx.put("vertxDependencies", vertxDependencies);

    Path tempDirPath = tempDir.path();
    String tempDirPathStr = tempDirPath.toString();

    copy(tempDir, "_editorconfig");
    copy(tempDir, "_gitignore");

    if (project.getBuildTool() == BuildTool.GRADLE) {
      copyDir(tempDir, "gradle");
      render(tempDir, ctx, "build.gradle");
      render(tempDir, ctx, "settings.gradle");
    } else if (project.getBuildTool() == BuildTool.MAVEN) {
      copyDir(tempDir, "maven");
      render(tempDir, ctx, "pom.xml");
    } else {
      throw new RuntimeException("Unsupported build tool: " + project.getBuildTool());
    }

    if (project.getLanguage() == Language.KOTLIN) {
      render(tempDir, ctx, "src/main/kotlin/io/vertx/starter/MainVerticle.kt");
      render(tempDir, ctx, "src/test/kotlin/io/vertx/starter/TestMainVerticle.kt");
    } else if (project.getLanguage() == Language.JAVA) {
      render(tempDir, ctx, "src/main/java/io/vertx/starter/MainVerticle.java");
      render(tempDir, ctx, "src/test/java/io/vertx/starter/TestMainVerticle.java");
    } else {
      throw new RuntimeException("Unsupported language: " + project.getLanguage());
    }

    render(tempDir, ctx, "README.adoc");
  }

  private void copy(TempDir tempDir, String filename) throws IOException {
    Path dest = tempDir.path().resolve(filename);
    Files.createDirectories(dest.getParent());
    vertx.fileSystem().copyBlocking("files/" + filename, dest.toString());
  }

  private void copyDir(TempDir tempDir, String dirname) {
    vertx.fileSystem().copyRecursiveBlocking("files/" + dirname, tempDir.path().toString(), true);
  }

  private void render(TempDir tempDir, Map<String, Object> ctx, String filename) throws IOException {
    Path dest = tempDir.path().resolve(filename);
    Files.createDirectories(dest.getParent());
    Buffer data = renderBlocking(ctx, "templates/" + filename + ".ftl");
    vertx.fileSystem().writeFileBlocking(dest.toString(), data);
  }

  private Buffer renderBlocking(Map<String, Object> context, String templateFileName) {
    CompletableFuture<Buffer> cf = new CompletableFuture<>();
    templateEngine.render(context, templateFileName, ar -> {
      if (ar.succeeded()) {
        cf.complete(ar.result());
      } else {
        cf.completeExceptionally(ar.cause());
      }
    });
    try {
      return cf.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e.getCause());
    }
  }

  private void generateArchive(TempDir tempDir, ArchiveOutputStream stream) throws IOException {
    Files.walk(tempDir.path()).forEach(filePath -> {
      try {
        addFile(tempDir.path(), filePath, stream);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private void addFile(Path rootPath, Path filePath, ArchiveOutputStream stream) throws IOException {
    String relativePath = rootPath.relativize(filePath).toString();
    if (relativePath.length() == 0) return;
    String entryName = relativePath.charAt(0) == '_' ? '.' + relativePath.substring(1) : relativePath;
    ArchiveEntry entry = stream.createArchiveEntry(filePath.toFile(), entryName);
    if (filePath.toFile().isFile() && filePath.toFile().canExecute()) {
      if (entry instanceof ZipArchiveEntry) {
        ZipArchiveEntry zipArchiveEntry = (ZipArchiveEntry) entry;
        zipArchiveEntry.setUnixMode(0744);
      } else if (entry instanceof TarArchiveEntry) {
        TarArchiveEntry tarArchiveEntry = (TarArchiveEntry) entry;
        tarArchiveEntry.setMode(0100744);
      }
    }
    stream.putArchiveEntry(entry);
    if (filePath.toFile().isFile()) {
      try (InputStream i = Files.newInputStream(filePath)) {
        IOUtils.copy(i, stream);
      }
    }
    stream.closeArchiveEntry();
  }

  @FunctionalInterface
  private interface ArchiveOutputStreamFactory {
    ArchiveOutputStream create(ByteArrayOutputStream baos) throws IOException;
  }
}