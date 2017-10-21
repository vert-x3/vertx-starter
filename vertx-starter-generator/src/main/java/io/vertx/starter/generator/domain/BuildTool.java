package io.vertx.starter.generator.domain;

import static java.lang.String.format;

public interface BuildTool {

  String mainSourcesDir();

  String mainResourcesDir();

  String testSourcesDir();

  String testResourcesDir();

  class Maven implements BuildTool {

    private final String language;

    public Maven(String language) {
      this.language = language;
    }

    public String mainSourcesDir() {
      return format("src/main/%s/", this.language);
    }

    public String mainResourcesDir() {
      return "src/main/resources/";
    }

    public String testSourcesDir() {
      return format("src/main/%s/", this.language);
    }

    public String testResourcesDir() {
      return "src/test/resources/";
    }
  }

}
