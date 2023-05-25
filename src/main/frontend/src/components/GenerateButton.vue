<!--
  - Copyright 2023 Red Hat, Inc.
  -
  - Red Hat licenses this file to you under the Apache License, version 2.0
  - (the "License"); you may not use this file except in compliance with the
  - License.  You may obtain a copy of the License at:
  -
  - http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  - WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  - License for the specific language governing permissions and limitations
  - under the License.
  -->

<script>
import { store } from '@/store'
import Bowser from 'bowser'
import { CliConstants } from '@/cli'
import { saveAs } from 'file-saver'
import ClipboardJS from 'clipboard'
import { Tooltip } from 'bootstrap'
import hotkeys from 'hotkeys-js'

const parsedResult = Bowser.parse(window.navigator.userAgent)

export default {
  data() {
    return {
      store
    }
  },
  computed: {
    isWindows() {
      return parsedResult.os.name.toLowerCase() === 'windows'
    },
    isMac() {
      return parsedResult.os.name.toLowerCase() === 'macOS'
    },
    hotkey() {
      return this.isMac ? '\u2318 + \u23CE' : 'alt + \u23CE'
    },
    projectRequest() {
      const vertxProject = {}
      Object.assign(vertxProject, store.project)
      const artifacts = vertxProject.vertxDependencies.map((dependency) => dependency.artifactId)
      vertxProject.vertxDependencies = artifacts.join()
      return vertxProject
    },
    powershellCommand() {
      return this.renderCommand(
        CliConstants.commands.powershell,
        CliConstants.args,
        this.projectRequest
      )
    },
    curlCommand() {
      return this.renderCommand(CliConstants.commands.curl, CliConstants.args, this.projectRequest)
    },
    httpieCommand() {
      return this.renderCommand(
        CliConstants.commands.httpie,
        CliConstants.args,
        this.projectRequest
      )
    }
  },
  methods: {
    renderCommand(command, commandArgs, projectRequest) {
      return command.template.format({
        baseUrl: window.location,
        archiveFormat: projectRequest.archiveFormat,
        args: this.toCliArgs(projectRequest, commandArgs, command.argMapper, command.argSeparator),
        artifactId: projectRequest.artifactId
      })
    },
    toCliArgs(obj, commandArgs, argMapper, argSeparator) {
      const args = []
      commandArgs.forEach((argName) => {
        if (
          Object.prototype.hasOwnProperty.call(obj, argName) &&
          obj[argName] !== undefined &&
          obj[argName].trim().length > 0
        ) {
          args.push(argMapper(argName, obj[argName]))
        }
      })
      return args.join(argSeparator)
    },
    generate() {
      store.isGenerating = true
      const url =
        '/starter.' +
        this.projectRequest.archiveFormat +
        '?' +
        new URLSearchParams({
          groupId: this.projectRequest.groupId,
          artifactId: this.projectRequest.artifactId,
          language: this.projectRequest.language,
          buildTool: this.projectRequest.buildTool,
          vertxVersion: this.projectRequest.vertxVersion,
          vertxDependencies: this.projectRequest.vertxDependencies,
          packageName: this.projectRequest.packageName,
          jdkVersion: this.projectRequest.jdkVersion
        }).toString()
      fetch(url, {
        method: 'GET',
        cache: 'no-cache'
      })
        .then((response) => {
          response.blob().then((blob) => {
            store.isGenerating = false
            if (response.ok) {
              saveAs(blob, this.projectRequest.artifactId + '.' + this.projectRequest.archiveFormat)
            } else {
              const reader = new FileReader()
              reader.addEventListener('loadend', () => {
                store.alerts.push({
                  message:
                    reader.result.length > 0
                      ? JSON.parse(reader.result).message
                      : 'Server replied with error without details.',
                  type: response.status >= 400 && response.status < 500 ? 'warning' : 'danger'
                })
              })
              reader.readAsText(blob, 'utf8')
            }
          })
        })
        .catch((error) => {
          store.isGenerating = false
          store.alerts.push({
            message: error.message,
            type: 'danger'
          })
        })
    }
  },
  mounted() {
    new ClipboardJS('.btn-clip')
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    tooltipTriggerList.forEach((tooltipTriggerEl) => new Tooltip(tooltipTriggerEl))
    hotkeys('command+enter,alt+enter', (event) => {
      event.preventDefault()
      this.generate()
    })
  }
}
</script>

<template>
  <div class="row mt-4">
    <div class="col-sm-12 text-center">
      <div class="btn-group">
        <button type="button" class="btn btn-lg btn-primary" @click="generate">
          Generate Project <kbd>{{ hotkey }}</kbd>
        </button>
        <button
          type="button"
          class="btn btn-lg btn-outline-secondary dropdown-toggle dropdown-toggle-split"
          data-bs-toggle="dropdown"
          aria-expanded="false"
        >
          <i class="bi-terminal"></i>
        </button>
        <ul v-if="isWindows" class="dropdown-menu dropdown-menu-end mx-2">
          <li class="mx-3 my-2">
            <label><strong>Generate with Power Shell</strong></label>
            <div class="input-group my-2">
              <input
                id="powershellCommand"
                :value="powershellCommand"
                type="text"
                class="form-control"
                readonly
                aria-label="Project Power Shell URL"
              />
              <button
                class="btn btn-secondary btn-clip"
                type="button"
                data-clipboard-target="#powershellCommand"
                data-bs-toggle="tooltip"
                data-bs-placement="right"
                data-bs-title="Copy to clipboard"
              >
                <i class="bi-clipboard"></i>
              </button>
            </div>
          </li>
        </ul>
        <ul v-else class="dropdown-menu dropdown-menu-end">
          <li class="mx-3 my-2">
            <label><strong>Generate with cURL</strong></label>
            <div class="input-group my-2">
              <input
                id="curlCommand"
                :value="curlCommand"
                type="text"
                class="form-control"
                readonly
                aria-label="Project cURL URL"
              />
              <button
                class="btn btn-secondary btn-clip"
                type="button"
                data-clipboard-target="#curlCommand"
                data-bs-toggle="tooltip"
                data-bs-placement="right"
                data-bs-title="Copy to clipboard"
              >
                <i class="bi-clipboard"></i>
              </button>
            </div>
          </li>
          <li class="mx-3 my-2">
            <label><strong>Generate with HTTPie</strong></label>
            <div class="input-group my-2">
              <input
                id="httpieCommand"
                :value="httpieCommand"
                type="text"
                class="form-control"
                readonly
                aria-label="Project HTTPie URL"
              />
              <button
                class="btn btn-secondary btn-clip"
                type="button"
                data-clipboard-target="#httpieCommand"
                data-bs-toggle="tooltip"
                data-bs-placement="right"
                data-bs-title="Copy to clipboard"
              >
                <i class="bi-clipboard"></i>
              </button>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>
  <div class="row mt-2" v-if="store.isGenerating">
    <div class="col-sm-1 mx-auto">
      <div class="spinner-grow text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dropdown-toggle::after {
  display: none;
}

.dropdown-menu {
  width: 20em;
}

input:read-only {
  background-color: lightgray;
}

kbd {
  background-color: transparent;
}
</style>
