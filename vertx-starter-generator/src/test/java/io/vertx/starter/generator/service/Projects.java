package io.vertx.starter.generator.service;

import io.vertx.starter.generator.domain.ProjectFiles;
import io.vertx.starter.generator.domain.impl.BasicProjectFiles;
import io.vertx.starter.generator.domain.impl.MavenJavaProjectFiles;
import io.vertx.starter.generator.utils.TestProjectBuilder;

import java.util.stream.Stream;

public class Projects {

    private static final ProjectFiles BASIC_PROJECT_FILES = new BasicProjectFiles();

    public static ProjectAndProjectFiles mavenJava(String baseDir, String groupId, String artifactId, String version) {
        return new ProjectAndProjectFiles(
            new TestProjectBuilder(baseDir).groupId(groupId).artifactId(artifactId).version(version).java().maven().build(),
            Stream.concat(
                BASIC_PROJECT_FILES.files(),
                new MavenJavaProjectFiles(groupId, artifactId).files()
            )
        );
    }



}
