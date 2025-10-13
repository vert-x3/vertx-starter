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
import FooterComponent from '@/components/FooterComponent.vue'
import SelectedDependencies from '@/components/SelectedDependencies.vue'
import AdvancedOptions from '@/components/AdvancedOptions.vue'
</script>

<script>
import { store } from '@/store'

export default {
  data() {
    return {
      store
    }
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
        @value-changed="store.onLanguageChanged"
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
      <AdvancedOptions />
      <AlertsPanel />
      <GenerateButton />
      <SelectedDependencies />
      <FooterComponent />
    </form>
  </div>
</template>
