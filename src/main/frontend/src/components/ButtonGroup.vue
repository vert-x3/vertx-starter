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
import { startCase as _startCase } from 'lodash'
import { store } from '@/store'

export default {
  props: {
    values: {
      type: Array,
      required: true
    },
    formLabel: {
      type: String,
      required: true
    },
    projectProperty: {
      type: String,
      required: true
    },
    prefix: {
      type: String,
      default: ''
    },
    capitalize: Boolean
  },
  data() {
    return {
      store
    }
  },
  methods: {
    inputId(i) {
      return this.projectProperty + '-' + i
    },
    formatRadioLabel(val) {
      return this.prefix + (this.capitalize ? _startCase(val) : val)
    }
  }
}
</script>

<template>
  <div class="row mt-3">
    <label class="col-sm-4 col-form-label"
      ><strong>{{ formLabel }}</strong></label
    >
    <div class="col-sm-8">
      <div class="btn-group" role="group">
        <template v-for="(value, i) in values" :key="value">
          <input
            type="radio"
            class="btn-check"
            :id="inputId(i)"
            :name="projectProperty"
            :value="value"
            autocomplete="off"
            v-model="store.project[projectProperty]"
            :checked="value === store.project[projectProperty]"
            @change="$emit('valueChanged')"
            :disabled="store.isProjectPropertyDisabled(projectProperty, value)"
          />
          <label class="btn btn-outline-primary" :for="inputId(i)">{{
            formatRadioLabel(value)
          }}</label>
        </template>
      </div>
    </div>
  </div>
</template>
