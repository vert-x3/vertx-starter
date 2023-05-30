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
  <div id="advancedAnchor" class="row mt-4">
    <div class="col-sm-12">
      <p class="text-center mb-0">
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
</template>
