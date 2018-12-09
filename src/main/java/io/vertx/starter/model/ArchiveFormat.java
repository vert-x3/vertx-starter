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

public enum ArchiveFormat {

  @JsonProperty("zip")
  ZIP("zip", "application/zip"),

  @JsonProperty("tgz")
  TGZ("tar.gz", "application/zip");

  private final String fileExtension;
  private final String contentType;

  ArchiveFormat(String fileExtension, String contentType) {
    this.contentType = contentType;
    this.fileExtension = fileExtension;
  }

  public static ArchiveFormat fromFilename(String filename) {
    if (filename.matches(".*\\.zip$")) {
      return ArchiveFormat.ZIP;
    }
    if (filename.matches(".*(\\.tar\\.gz|\\.tgz)$")) {
      return ArchiveFormat.TGZ;
    }
    return null;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  public String getContentType() {
    return contentType;
  }
}
