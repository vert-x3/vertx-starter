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
import { scrollTo } from '@/scroll'

export default {
  data() {
    return {
      store,
      selectedDependency: '',
      collapsed: true,
      selectedPanel: store.stack[0].code
    }
  },
  computed: {
    searchDependencies() {
      const sd = this.selectedDependency
      const res = []
      if (sd === '') {
        return res
      }
      for (const avd of store.availableVertxDependencies.value) {
        if (avd.name.toLowerCase().includes(sd.toLowerCase()) && res.length < 8) {
          res.push(avd)
        }
      }
      return res
    }
  },
  methods: {
    isDependencyDisabled(dependency) {
      const excludedOfVersion = store.excludedModulesByVersion[store.project.vertxVersion]
      return excludedOfVersion.includes(dependency.artifactId)
    },
    isDependencySelected(dependency) {
      return store.project.vertxDependencies.includes(dependency)
    },
    onDependencySelected(dependency) {
      this.selectedDependency = ''
      store.addDependency(dependency)
    },
    onDependencyClicked(event, dependency) {
      if (event.target.checked) {
        store.addDependency(dependency)
      } else {
        store.removeDependency(dependency)
      }
    }
  },
  mounted() {
    const dependencies = document.getElementById('dependencies')
    dependencies.addEventListener('blur', () => {
      setTimeout(
        function (self) {
          self.selectedDependency = ''
        },
        150,
        this
      )
    })
    const collapseDependenciesPanel = document.getElementById('collapseDependenciesPanel')
    collapseDependenciesPanel.addEventListener('show.bs.collapse', () => {
      this.collapsed = false
      scrollTo('detailedDependenciesAnchor')
    })
    collapseDependenciesPanel.addEventListener('hide.bs.collapse', () => {
      this.collapsed = true
      scrollTo('dependencyTypeaheadAnchor')
    })
  }
}
</script>

<template>
  <div id="dependencyTypeaheadAnchor" class="row mt-3">
    <label for="dependencies" class="col-sm-4 col-form-label">
      <strong
        >Dependencies ({{ store.project.vertxDependencies.length }}/{{
          store.availableVertxDependencies.length
        }})</strong
      >
    </label>
    <div class="col-sm-8">
      <input
        type="text"
        id="dependencies"
        class="form-control"
        placeholder="Web, MQTT, etc."
        v-model="selectedDependency"
      />
      <div class="mt-1" v-if="searchDependencies.length">
        <ul id="searchDependencies" class="shadow dropdown-menu show">
          <li v-for="dependency in searchDependencies" :key="dependency.artifactId">
            <a class="dropdown-item" @click="onDependencySelected(dependency)">{{
              dependency.name
            }}</a>
          </li>
        </ul>
      </div>
    </div>
  </div>
  <div class="row mt-3">
    <div class="col-sm-12">
      <p class="text-center">
        <button
          class="btn btn-link"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#collapseDependenciesPanel"
        >
          <strong>{{ collapsed ? 'Show dependencies panel' : 'Hide dependencies panel' }}</strong>
          &nbsp;
          <i
            :class="collapsed ? 'bi-plus-circle-fill' : 'bi-dash-circle-fill'"
            aria-hidden="true"
          ></i>
        </button>
      </p>
    </div>
  </div>
  <div id="collapseDependenciesPanel" class="collapse row mt-3">
    <div class="col-md-4">
      <h3 id="detailedDependenciesAnchor">Dependencies</h3>
      <ul class="nav nav-pills flex-column">
        <li class="nav-item" v-for="cat in store.stack" :key="cat.code">
          <a
            class="nav-link"
            :class="selectedPanel === cat.code ? 'active' : ''"
            data-bs-toggle="tab"
            :data-bs-target="'#tab-' + cat.code"
            role="tab"
            @click="selectedPanel = cat.code"
          >
            {{ cat.category }}
          </a>
        </li>
      </ul>
    </div>
    <div class="col-sm-8">
      <div class="tab-content" id="panel-content">
        <div
          v-for="cat in store.stack"
          :key="cat.code"
          :id="'tab-' + cat.code"
          class="tab-pane"
          role="tabpanel"
          :class="selectedPanel === cat.code ? 'active' : ''"
        >
          <h3>{{ cat.category }}</h3>
          <p>{{ cat.description }}</p>
          <div v-for="dependency in cat.items" :key="dependency.artifactId">
            <label :class="{ 'text-muted': isDependencyDisabled(dependency) }">
              <input
                type="checkbox"
                :disabled="isDependencyDisabled(dependency)"
                :checked="isDependencySelected(dependency)"
                @click="onDependencyClicked($event, dependency)"
              />
              &nbsp;<strong>{{ dependency.name }}</strong>
            </label>
            <p :class="{ 'text-muted': isDependencyDisabled(dependency) }">
              {{ dependency.description }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
