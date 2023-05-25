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
import AlertsPanel from '@/components/AlertsPanel.vue'
import GenerateButton from '@/components/GenerateButton.vue'
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
      collapseAdvancedIcon.className = 'bi-dash-lg'
      scrollTo('advancedAnchor')
    })
    collapseAdvanced.addEventListener('hide.bs.collapse', () => {
      collapseAdvancedIcon.className = 'bi-plus-lg'
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
              <i id="collapseAdvancedIcon" class="bi-plus-lg" aria-hidden="true"></i>
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
                <div class="btn-group mt-2">
                  <button type="button" class="btn btn-outline-primary disabled">
                    {{ dependency.name }}
                  </button>
                  <button
                    type="button"
                    class="btn btn-outline-primary"
                    :class="{ 'me-2': index !== store.project.vertxDependencies.length }"
                    @click="store.removeDependency(dependency)"
                  >
                    &times;
                  </button>
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>
      <AlertsPanel />
      <GenerateButton />
      <div class="row">
        <div class="col-sm-12">
          <p class="small text-end">
            <a href="https://how-to.vertx.io/"
              ><i class="bi-arrow-right" aria-hidden="true"></i> Find a Vert.x how-to</a
            >
          </p>
        </div>
      </div>
      <div class="row">
        <div class="col-sm-12">
          <p class="small text-end">
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
