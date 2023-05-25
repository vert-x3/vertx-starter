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
  props: {
    formLabel: {
      type: String,
      required: true
    },
    placeHolder: {
      type: String,
      required: true
    },
    projectProperty: {
      type: String,
      required: true
    },
    pattern: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      store
    }
  },
  computed: {
    regExp() {
      return new RegExp(this.pattern)
    },
    isInvalid() {
      let val = this.store.project[this.projectProperty]
      let type = typeof val
      return type !== 'string' || (val.trim().length > 0 && !this.regExp.test(val))
    }
  },
  watch: {
    isInvalid(value) {
      if (value) {
        store.invalidFields.value.add(this.projectProperty)
      } else {
        store.invalidFields.value.delete(this.projectProperty)
      }
    }
  },
  methods: {
    inputId(i) {
      return this.projectProperty + '-' + i
    }
  }
}
</script>

<template>
  <div class="row mt-3">
    <label for="groupId" class="col-sm-4 col-form-label">
      <strong>{{ formLabel }}</strong>
    </label>
    <div class="col-sm-8">
      <input
        :class="isInvalid ? 'form-control is-invalid' : 'form-control'"
        type="text"
        :id="projectProperty"
        :name="projectProperty"
        v-model="store.project[projectProperty]"
        :placeholder="placeHolder"
      />
      <div class="invalid-feedback" v-if="isInvalid">Invalid format</div>
    </div>
  </div>
</template>
