/*
 * Copyright 2019 Red Hat, Inc.
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

package io.vertx.starter.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Thomas Segismont
 */
public enum JdkVersion {
  @JsonProperty("1.8")
  JDK_1_8("1.8"),
  @JsonProperty("11")
  JDK_11("11");

  private final String value;

  JdkVersion(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static JdkVersion fromString(String str) {
    for (JdkVersion jdkVersion : values()) {
      if (jdkVersion.getValue().equals(str)) {
        return jdkVersion;
      }
    }
    throw new IllegalArgumentException(str);
  }
}
