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
import io.vertx.ext.web.templ.freemarker.FreeMarkerTemplateEngine;
import io.vertx.starter.model.ArchiveFormat;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.vertx.starter.model.BuildTool.GRADLE;
import static io.vertx.starter.model.BuildTool.MAVEN;
import static io.vertx.starter.model.Language.KOTLIN;
import static java.util.stream.Collectors.joining;

public class GeneratorService {

  private static final Logger log = LogManager.getLogger(GeneratorService.class);

  private static final Pattern DOT_REGEX = Pattern.compile("\\.");

  private static final Set<String> EXECUTABLES = Set.of(
    ".mvn/wrapper/maven-wrapper.jar",
    ".mvn/wrapper/maven-wrapper.properties",
    "mvnw",
    "gradlew");

  private final Vertx vertx;
  private final Set<String> keywords;
  private final FreeMarkerTemplateEngine templateEngine;


  public GeneratorService(Vertx vertx, Set<String> keywords) {
    this.vertx = vertx;
    this.keywords = keywords;
    templateEngine = FreeMarkerTemplateEngine.create(vertx);
  }

  public Buffer onProjectRequested(VertxProject project) throws Exception {
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
    log.trace("Generating project: {}", project);

    Map<String, Object> ctx = new HashMap<>();
    ctx.put("buildTool", project.getBuildTool().name().toLowerCase());
    String groupId = project.getGroupId();
    ctx.put("groupId", groupId);
    String artifactId = project.getArtifactId();
    ctx.put("artifactId", artifactId);
    Language language = project.getLanguage();
    ctx.put("language", language.name().toLowerCase());
    ctx.put("vertxVersion", project.getVertxVersion());
    Set<String> vertxDependencies = project.getVertxDependencies();
    if (vertxDependencies == null) {
      vertxDependencies = new HashSet<>();
    }
    boolean hasVertxUnit = vertxDependencies.remove("vertx-unit");
    ctx.put("hasVertxUnit", hasVertxUnit);
    boolean hasVertxJUnit5 = vertxDependencies.remove("vertx-junit5") || !hasVertxUnit;
    ctx.put("hasVertxJUnit5", hasVertxJUnit5);
    if (hasVertxUnit && hasVertxJUnit5) {
      throw new RuntimeException("You cannot generate a project which depends on both vertx-unit and vertx-junit5.");
    }
    boolean hasPgClient = vertxDependencies.contains("vertx-pg-client");
    ctx.put("hasPgClient", hasPgClient);
    vertxDependencies.addAll(language.getLanguageDependencies());
    ctx.put("vertxDependencies", vertxDependencies);
    String packageName = packageName(project);
    ctx.put("packageName", packageName);
    ctx.put("jdkVersion", project.getJdkVersion().getValue());
    ctx.put("futurizedVerticle", project.getVertxVersion().startsWith("5."));

    copy(tempDir, "files", "_editorconfig");
    copy(tempDir, "files", "_gitignore");

    if (project.getBuildTool() == GRADLE) {
      copy(tempDir, "files/gradle", "gradlew");
      copy(tempDir, "files/gradle", "gradlew.bat");
      if (project.getLanguage() == KOTLIN) {
        copy(tempDir, "files/gradle", "gradle.properties");
      }
      copy(tempDir, "files/gradle", "gradle/wrapper/gradle-wrapper.jarr");
      copy(tempDir, "files/gradle", "gradle/wrapper/gradle-wrapper.properties");
      render(tempDir, ctx, ".", "build.gradle.kts");
      render(tempDir, ctx, ".", "settings.gradle.kts");
    } else if (project.getBuildTool() == MAVEN) {
      copy(tempDir, "files/maven", "mvnw");
      copy(tempDir, "files/maven", "mvnw.cmd");
      copy(tempDir, "files/maven", "_mvn/wrapper/maven-wrapper.jarr");
      copy(tempDir, "files/maven", "_mvn/wrapper/maven-wrapper.properties");
      render(tempDir, ctx, ".", "pom.xml");
    } else {
      throw new RuntimeException("Unsupported build tool: " + project.getBuildTool());
    }

    String packageDir = packageName.replace('.', '/');
    String srcDir = "src/main/" + language.getName();
    render(tempDir, ctx, srcDir, "MainVerticle" + language.getExtension(), srcDir + "/" + packageDir);
    String testSrcDir = "src/test/" + language.getName();
    render(tempDir, ctx, testSrcDir, "TestMainVerticle" + language.getExtension(), testSrcDir + "/" + packageDir);

    render(tempDir, ctx, ".", "README.adoc");
  }

  private String packageName(VertxProject project) {
    String packageName = project.getPackageName();
    if (packageName != null && !packageName.trim().isEmpty()) {
      return packageName;
    }
    String groupId = project.getGroupId();
    String artifactId = project.getArtifactId();
    return Stream.concat(DOT_REGEX.splitAsStream(groupId), DOT_REGEX.splitAsStream(artifactId))
      .map(s -> s.replace("-", "_"))
      .map(s -> keywords.contains(s) ? s + "_" : s)
      .map(s -> Character.isJavaIdentifierStart(s.codePointAt(0)) ? s : "_" + s)
      .collect(joining("."));
  }

  private void copy(TempDir tempDir, String base, String filename) throws IOException {
    Path dest = tempDir.path().resolve(filename);
    Files.createDirectories(dest.getParent());
    vertx.fileSystem().copyBlocking(base + "/" + filename, dest.toString());
  }

  @SuppressWarnings("SameParameterValue")
  private void render(TempDir tempDir, Map<String, Object> ctx, String sourceDir, String filename) throws IOException {
    render(tempDir, ctx, sourceDir, filename, sourceDir);
  }

  private void render(TempDir tempDir, Map<String, Object> ctx, String sourceDir, String filename, String destDir) throws IOException {
    Path dest = tempDir.path().resolve(destDir);
    Files.createDirectories(dest);
    Buffer data = renderBlocking(ctx, "templates/" + sourceDir + "/" + filename + ".ftl");
    vertx.fileSystem().writeFileBlocking(dest.resolve(filename).toString(), data);
  }

  private Buffer renderBlocking(Map<String, Object> context, String templateFileName) {
    try {
      return templateEngine.render(context, templateFileName)
        .toCompletionStage()
        .toCompletableFuture()
        .get();
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

  @SuppressWarnings("OctalInteger")
  private void addFile(Path rootPath, Path filePath, ArchiveOutputStream stream) throws IOException {
    String relativePath = rootPath.relativize(filePath).toString();
    if (relativePath.length() == 0) return;
    String entryName = jarFileWorkAround(leadingDot(relativePath));
    ArchiveEntry entry = stream.createArchiveEntry(filePath.toFile(), entryName);
    if (EXECUTABLES.contains(entryName)) {
      if (entry instanceof ZipArchiveEntry zipArchiveEntry) {
        zipArchiveEntry.setUnixMode(0744);
      } else if (entry instanceof TarArchiveEntry tarArchiveEntry) {
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

  private String leadingDot(String s) {
    return s.charAt(0) == '_' ? '.' + s.substring(1) : s;
  }

  private String jarFileWorkAround(String s) {
    // See https://github.com/johnrengelman/shadow/issues/111
    return s.endsWith(".jarr") ? s.substring(0, s.length() - ".jarr".length()) + ".jar" : s;
  }

  @FunctionalInterface
  private interface ArchiveOutputStreamFactory {
    ArchiveOutputStream create(ByteArrayOutputStream baos) throws IOException;
  }
}
