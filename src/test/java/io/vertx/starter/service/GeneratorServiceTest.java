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
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.starter.model.ArchiveFormat;
import io.vertx.starter.model.BuildTool;
import io.vertx.starter.model.Language;
import io.vertx.starter.model.VertxProject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static io.vertx.starter.config.ProjectConstants.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
public class GeneratorServiceTest {

  public static final String BASE_GENERATOR_DIR = "src/test/resources/generator";
  public static final String GENERATOR_OUTPUT_DIR = "build";
  private static final Logger log = LoggerFactory.getLogger(GeneratorServiceTest.class);

  private static VertxProject initProject() {
    VertxProject project = new VertxProject();
    project.setId(UUID.randomUUID().toString());
    project.setType("AAAAA");
    project.setGroupId("AA.BB.CC");
    project.setArtifactId("DD");
    project.setLanguage(Language.JAVA);
    project.setBuildTool(BuildTool.MAVEN);
    project.setVertxVersion("0.0.0");
    project.setVertxDependencies(new HashSet<>(asList("EE", "FF")));
    project.setArchiveFormat(ArchiveFormat.ZIP);
    return project;
  }

  @Test
  @DisplayName("should return archive filename when Gradle is done")
  public void should(Vertx vertx, VertxTestContext testContext) {
    VertxProject project = initProject();
    String generatorDir = Paths.get(BASE_GENERATOR_DIR, "no-op").toString();
    String generatorOutputDir = Paths.get(generatorDir, "/build").toString();
    GeneratorService generatorService = new GeneratorService(generatorDir, generatorOutputDir, vertx);
    generatorService.generateProject(project, testContext.succeeding(generatedArchive -> testContext.verify(() -> {
      String expectedArchiveName = Paths.get(generatorOutputDir, project.getId(), project.getArtifactId() + "." + project.getArchiveFormat().getFileExtension()).toAbsolutePath().toString();
      assertThat(generatedArchive).isEqualTo(expectedArchiveName);
      testContext.completeNow();
    })));
  }

  @ParameterizedTest
  @EnumSource(Language.class)
  @DisplayName("should passed project properties to Gradle")
  public void shouldTriggerGradleGeneration(Language language, Vertx vertx, VertxTestContext testContext) {
    VertxProject project = initProject();
    project.setLanguage(language);
    String generatorDir = Paths.get(BASE_GENERATOR_DIR, "passing-properties").toString();
    GeneratorService generatorService = new GeneratorService(generatorDir, GENERATOR_OUTPUT_DIR, vertx);

    generatorService.generateProject(project, testContext.succeeding(generatedArchive -> testContext.verify(() -> {
      String filename = Paths.get(GENERATOR_OUTPUT_DIR, project.getId(), "project.json").toString();
      vertx.fileSystem().readFile(filename, testContext.succeeding(buffer -> testContext.verify(() -> {
        JsonObject generatedProject = new JsonObject(buffer);
        assertThat(generatedProject.getString(TYPE)).isEqualTo(project.getType());
        assertThat(generatedProject.getString(GROUP_ID)).isEqualTo(project.getGroupId());
        assertThat(generatedProject.getString(ARTIFACT_ID)).isEqualTo(project.getArtifactId());
        assertThat(generatedProject.getString(LANGUAGE)).isEqualTo(project.getLanguage().toString());
        assertThat(generatedProject.getString(BUILD_TOOL)).isEqualTo(project.getBuildTool().toString());
        assertThat(generatedProject.getString(VERTX_VERSION)).isEqualTo(project.getVertxVersion());
        // Should be parsed as JsonArray and not String
        Set<String> expectedDependencies = project.getVertxDependencies();
        expectedDependencies.addAll(language.getLanguageDependencies());
        assertThat(generatedProject.getString(VERTX_DEPENDENCIES).split(",")).containsAll(expectedDependencies);
        assertThat(generatedProject.getString(ARCHIVE_FORMAT)).isEqualTo(project.getArchiveFormat().getFileExtension());
        testContext.completeNow();
      })));
    })));
  }

}
