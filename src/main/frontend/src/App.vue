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

<script setup>
import ButtonGroup from '@/components/ButtonGroup.vue'
import DependenciesPanels from '@/components/DependenciesPanels.vue'
import ValidatedInput from '@/components/ValidatedInput.vue'
</script>

<script>
import { store } from '@/store'
import { scrollTo } from '@/scroll'

export default {
  data() {
    return {
      store
    }
  },
  mounted() {
    const collapseAdvanced = document.getElementById('collapseAdvanced')
    const collapseAdvancedIcon = document.getElementById('collapseAdvancedIcon')
    collapseAdvanced.addEventListener('show.bs.collapse', () => {
      collapseAdvancedIcon.className = 'bi-dash-circle-fill'
      scrollTo('advancedAnchor')
    })
    collapseAdvanced.addEventListener('hide.bs.collapse', () => {
      collapseAdvancedIcon.className = 'bi-plus-circle-fill'
      store.resetAdvanced()
      scrollTo('dependencyTypeaheadAnchor')
    })
  }
}
</script>

<template>
  <div class="container">
    <form name="form" novalidate @submit.prevent="">
      <div class="row mt-4 mb-5">
        <div class="col-sm-12">
          <h2>Create a new Vert.x application</h2>
        </div>
      </div>
      <ButtonGroup
        form-label="Version"
        project-property="vertxVersion"
        :values="store.vertxVersions"
        @value-changed="store.onVersionChanged"
      />
      <ButtonGroup
        form-label="Language"
        project-property="language"
        :values="store.languages"
        capitalize
      />
      <ButtonGroup
        form-label="Build"
        project-property="buildTool"
        :values="store.buildTools"
        capitalize
      />
      <ValidatedInput
        form-label="Group Id"
        place-holder="Your project group id"
        project-property="groupId"
        pattern="^[A-Za-z0-9_\-.]+$"
      />
      <ValidatedInput
        form-label="Artifact Id"
        place-holder="Your project artifact id"
        project-property="artifactId"
        pattern="^[A-Za-z0-9_\-.]+$"
      />
      <DependenciesPanels />
      <div id="advancedAnchor" class="row mt-4">
        <div class="col-sm-12">
          <p class="text-center">
            <button
              class="btn btn-link"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#collapseAdvanced"
            >
              <strong>Advanced options</strong>
              &nbsp;
              <i id="collapseAdvancedIcon" class="bi-plus-circle-fill" aria-hidden="true"></i>
            </button>
          </p>
        </div>
      </div>
      <div class="collapse" id="collapseAdvanced">
        <hr />
        <ValidatedInput
          form-label="Package"
          place-holder="Your project package name"
          project-property="packageName"
          pattern="^[A-Za-z0-9_\-.]+$"
        />
        <ButtonGroup
          form-label="JDK Version"
          project-property="jdkVersion"
          :values="store.jdkVersions"
          prefix="JDK "
        />
        <hr />
      </div>
      <div class="row" v-if="store.project.vertxDependencies.length !== 0">
        <div class="col-sm-12">
          <div>
            <label>
              <strong>
                Selected dependencies ({{ store.project.vertxDependencies.length }})
              </strong>
            </label>
            <br />
            <div>
              <template
                v-for="(dependency, index) in store.project.vertxDependencies"
                :key="dependency"
              >
                <div
                  class="btn btn-outline-primary mt-2"
                  :class="{ 'me-2': index !== store.project.vertxDependencies.length }"
                >
                  {{ dependency.name }} dependency
                  <a @click="store.removeDependency(dependency)">&times;</a>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
      <div class="row">
        <div
          uib-alert
          ng-repeat="alert in vm.alerts track by $index"
          ng-class="'alert-' + (alert.type || 'warning')"
          close="vm.closeAlert($index)"
        >
          [[alert.message]]<span ng-if="alert.type === 'danger'"
            ><br />Please
            <a target="_blank" ng-href="https://github.com/vert-x3/vertx-starter/issues/new"
              >report</a
            >
            an issue.</span
          >
        </div>
        <div class="col-sm-12 text-center">
          <div
            class="btn-group fix-btn-lg"
            uib-dropdown
            auto-close="outsideClick"
            on-toggle="vm.generateCommands(open)"
            ng-switch
            on="vm.isWindows"
          >
            <button
              type="button"
              class="btn btn-lg btn-primary"
              ng-disabled="form.$invalid"
              ng-click="vm.generate()"
            >
              Generate Project <kbd>[[ vm.hotkey ]]</kbd>
            </button>
            <button
              type="button"
              class="btn btn-lg btn-default"
              uib-dropdown-toggle
              ng-disabled="form.$invalid"
            >
              <i class="bi-terminal"></i>
              <span class="sr-only">CLI</span>
            </button>
            <ul
              ng-switch-when="true"
              class="p-3 dropdown-menu dropdown-menu-large pull-right"
              uib-dropdown-menu
              role="menu"
              aria-labelledby="split-button"
            >
              <li role="menuitem">
                <label class="label-bold"> Generate with Power Shell </label>
                <div class="input-group">
                  <input
                    type="text"
                    id="powershellCommand"
                    ng-model="vm.powershellCommand"
                    class="form-control"
                    readonly="readonly"
                    aria-label="Project Power Shell URL"
                  />
                  <span class="input-group-btn">
                    <button
                      class="btn btn-default"
                      type="button"
                      ngclipboard
                      data-clipboard-target="#powershellCommand"
                      uib-tooltip="Copied!"
                      tooltip-trigger="'focus'"
                      tooltip-popup-close-delay="2000"
                    >
                      <i class="bi-copy"></i>
                    </button>
                  </span>
                </div>
              </li>
            </ul>
            <ul
              ng-switch-default
              class="p-3 dropdown-menu dropdown-menu-large pull-right"
              uib-dropdown-menu
              role="menu"
              aria-labelledby="split-button"
            >
              <li role="menuitem">
                <label class="label-bold"> Generate with Curl </label>
                <div class="input-group">
                  <input
                    type="text"
                    id="curlCommand"
                    ng-model="vm.curlCommand"
                    class="form-control"
                    readonly="readonly"
                    aria-label="Project Curl URL"
                  />
                  <span class="input-group-btn">
                    <button
                      class="btn btn-default"
                      type="button"
                      ngclipboard
                      data-clipboard-target="#curlCommand"
                      uib-tooltip="Copied!"
                      tooltip-trigger="'focus'"
                      tooltip-popup-close-delay="2000"
                    >
                      <i class="bi-copy"></i>
                    </button>
                  </span>
                </div>
              </li>
              <li role="menuitem">
                <label class="label-bold"> Generate with HTTPie </label>
                <div class="input-group">
                  <input
                    type="text"
                    id="httpieCommand"
                    ng-model="vm.httpieCommand"
                    class="form-control"
                    readonly="readonly"
                    aria-label="Project HTTPie URL"
                  />
                  <span class="input-group-btn">
                    <button
                      class="btn btn-default"
                      type="button"
                      ngclipboard
                      data-clipboard-target="#httpieCommand"
                      uib-tooltip="Copied!"
                      tooltip-trigger="'focus'"
                      tooltip-popup-close-delay="2000"
                    >
                      <i class="bi-copy"></i>
                    </button>
                  </span>
                </div>
              </li>
            </ul>
          </div>
        </div>
        <div class="col-sm-12" ng-show="vm.isGenerating">
          <p class="text-center">
            <i class="bi-spinner fa-spin"></i>
          </p>
        </div>
      </div>
      <div class="row">
        <div class="col-sm-12">
          <p class="small pull-right">
            <a href="https://how-to.vertx.io/"
              ><i class="bi-arrow-right" aria-hidden="true"></i> Find a Vert.x how-to</a
            >
          </p>
        </div>
      </div>
      <div class="row">
        <div class="col-sm-12">
          <p class="small pull-right">
            <a href="https://github.com/vert-x3/vertx-starter/issues"
              ><i class="bi-bug"></i> Report an issue</a
            >
          </p>
        </div>
      </div>
    </form>
  </div>
</template>

<style scoped></style>
