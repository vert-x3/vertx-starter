package io.vertx.starter.generator.service;

import static java.lang.String.format;

public class ProjectUtils {

  public static String packageDir(String groupId, String artifactId) {
    return format("%s/%s/", groupId.replaceAll("\\.", "/"), artifactId);
  }

}
