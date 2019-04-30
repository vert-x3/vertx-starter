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

import java.util.Arrays;
import java.util.List;

public enum ArchiveFormat {

  @JsonProperty("zip")
  ZIP("application/zip", "zip"),

  @JsonProperty("tgz")
  TGZ("application/gzip", "tar.gz", ".tar.gz");

  private final List<String> fileExtensions;
  private final String contentType;

  ArchiveFormat(String contentType, String... extensions) {
    this.contentType = contentType;
    this.fileExtensions = Arrays.asList(extensions);
  }

  public static ArchiveFormat fromFilename(String filename) {
    String lc = filename.toLowerCase();
    for (ArchiveFormat format : values()) {
      if (format.fileExtensions.stream().anyMatch(lc::endsWith)) {
        return format;
      }
    }
    return null;
  }

  public String getFileExtension() {
    return fileExtensions.get(0);
  }

  public String getContentType() {
    return contentType;
  }
}
