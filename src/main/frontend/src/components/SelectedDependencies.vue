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

export default {
  data() {
    return {
      store
    }
  }
}
</script>

<template>
  <div class="row mt-4" v-if="store.project.vertxDependencies.length !== 0">
    <div class="col-sm-12">
      <div>
        <label>
          <strong> Selected dependencies ({{ store.project.vertxDependencies.length }}) </strong>
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
</template>
