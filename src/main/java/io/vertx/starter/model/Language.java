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

package io.vertx.starter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public enum Language {
  @JsonProperty("java")
  JAVA("java", ".java"),

  @JsonProperty("kotlin")
  KOTLIN("kotlin", ".kt", "vertx-lang-kotlin"),

  @JsonProperty("scala")
  SCALA("scala", ".scala");

  private final String name;
  private final String extension;
  private final Set<String> languageDependencies;

  Language(String name, String extension, String... languageDependencies) {
    this.name = name;
    this.extension = extension;
    this.languageDependencies = Set.of(languageDependencies);
  }

  public String getName() {
    return name;
  }

  public String getExtension() {
    return extension;
  }

  public Set<String> getLanguageDependencies() {
    return languageDependencies;
  }

  public static Language fromString(String str) {
    for (Language language : values()) {
      if (language.name.equalsIgnoreCase(str)) {
        return language;
      }
    }
    return null;
  }
}
