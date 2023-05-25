/*
 * Copyright 2023 Red Hat, Inc.
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

String.prototype.format =
  String.prototype.format ||
  function () {
    'use strict'
    var str = this.toString()
    if (arguments.length) {
      var t = typeof arguments[0]
      var key
      var args =
        'string' === t || 'number' === t ? Array.prototype.slice.call(arguments) : arguments[0]

      for (key in args) {
        str = str.replace(new RegExp('\\${' + key + '\\}', 'gi'), args[key])
      }
    }

    return str
  }

export const CliConstants = {
  commands: {
    curl: {
      name: 'curl',
      template:
        'curl -G ${baseUrl}starter.${archiveFormat} ${args} --output ${artifactId}.${archiveFormat}',
      argMapper: function (name, value) {
        return '-d "${0}=${1}"'.format(name, value)
      },
      argSeparator: ' '
    },
    httpie: {
      name: 'HTTPie',
      template:
        'http ${baseUrl}starter.${archiveFormat} ${args} --output ${artifactId}.${archiveFormat}',
      argMapper: function (name, value) {
        return '${0}==${1}'.format(name, value)
      },
      argSeparator: ' '
    },
    powershell: {
      name: 'Power Shell',
      template:
        'Invoke-WebRequest -Uri ${baseUrl}starter.${archiveFormat} -Body @{ ${args} } -OutFile ${artifactId}.${archiveFormat}',
      argMapper: function (name, value) {
        return "${0}='${1}'".format(name, value)
      },
      argSeparator: '; '
    }
  },
  args: [
    'groupId',
    'artifactId',
    'packageName',
    'vertxVersion',
    'vertxDependencies',
    'language',
    'jdkVersion',
    'buildTool'
  ]
}
