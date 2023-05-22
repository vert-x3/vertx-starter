<script setup>
import ButtonGroup from '@/components/ButtonGroup.vue'
import ValidatedInput from '@/components/ValidatedInput.vue'
</script>

<script>
import { store } from '@/store'

export default {
  props: {
    metadata: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      store,
      projectDefaults: this.metadata.defaults,
      exclusions: this.metadata.versions.reduce((res, value) => {
        res[value.number] = value.exclusions || []
        return res
      }, {}),
      stack: this.metadata.stack,
      buildTools: this.metadata.buildTools,
      languages: this.metadata.languages,
      jdkVersions: this.metadata.jdkVersions,
      selectedPanel:
        this.metadata.stack && this.metadata.stack.length > 0
          ? this.metadata.stack[0].code
          : 'none',
      vertxVersions: this.metadata.versions.map((version) => version.number)
    }
  },
  methods: {
    reset() {
      store.project.groupId = this.projectDefaults.groupId
      store.project.artifactId = this.projectDefaults.artifactId
      store.project.language = this.projectDefaults.language
      store.project.buildTool = this.projectDefaults.buildTool
      store.project.vertxVersion = this.projectDefaults.vertxVersion
      store.project.archiveFormat = this.projectDefaults.archiveFormat
      store.project.vertxDependencies = []
      this.resetAdvanced()
    },
    resetAdvanced: function () {
      store.project.packageName = ''
      store.project.jdkVersion = this.projectDefaults.jdkVersion
    },
    scrollTo: function (elementId) {
      setTimeout(function () {
        const htmlElement = document.getElementById(elementId)
        if (htmlElement)
          htmlElement.scrollIntoView({ block: 'start', inline: 'nearest', behavior: 'smooth' })
      }, 150)
    }
  },
  created() {
    this.reset()
  },
  mounted() {
    const collapseAdvanced = document.getElementById('collapseAdvanced')
    const collapseAdvancedIcon = document.getElementById('collapseAdvancedIcon')
    collapseAdvanced.addEventListener('show.bs.collapse', () => {
      collapseAdvancedIcon.className = 'bi-dash-circle-fill'
      this.scrollTo('advancedAnchor')
    })
    collapseAdvanced.addEventListener('hide.bs.collapse', () => {
      collapseAdvancedIcon.className = 'bi-plus-circle-fill'
      this.resetAdvanced()
      this.scrollTo('dependencyTypeaheadAnchor')
    })
  }
}
</script>

<template>
  <div class="container">
    <form name="form" novalidate @submit.prevent="onSubmit">
      <div id="homeAnchor" class="row mt-4 mb-5">
        <div class="col-sm-12">
          <h2>Create a new Vert.x application</h2>
        </div>
      </div>
      <ButtonGroup form-label="Version" project-property="vertxVersion" :values="vertxVersions" />
      <ButtonGroup
        form-label="Language"
        project-property="language"
        :values="languages"
        capitalize
      />
      <ButtonGroup
        form-label="Build"
        project-property="buildTool"
        :values="buildTools"
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
      <div class="row mt-4">
        <div class="col-sm-12">
          <div id="dependencyTypeaheadAnchor" class="form-group row">
            <div class="col-sm-4">
              <label for="dependencies" class="col-form-label"
                >Dependencies
                ([[store.project.vertxDependencies.length]]/[[vm.availableVertxDependencies.length]])</label
              >
              <button
                type="button"
                class="btn btn-link btn-advanced"
                ng-click="vm.toggleDetailedOptions()"
                style="padding-left: 0px"
              >
                <i ng-class="vm.detailedOptionsCollapsed ? 'bi-plus' : 'bi-minus'"></i>
                [[vm.detailedOptionsCollapsed?"Show dependencies panel":"Hide dependencies panel"]]
              </button>
            </div>

            <div class="col-sm-8">
              <input
                type="text"
                id="dependencies"
                class="form-control"
                placeholder="Web, MQTT, etc."
                ng-model="vm.selectedDependency"
                uib-typeahead="dependency as dependency.name for dependency in vm.availableVertxDependencies | filter:$viewValue | limitTo:8"
                typeahead-on-select="vm.onDependencySelected($item, $model, $label, $event)"
              />
            </div>
          </div>
          <div id="detailedDependenciesAnchor" class="row" ng-hide="vm.detailedOptionsCollapsed">
            <div class="col-md-4">
              <h3>
                Dependencies
                ([[store.project.vertxDependencies.length]]/[[vm.availableVertxDependencies.length]])
              </h3>
              <ul class="nav nav-pills nav-stacked">
                <li
                  role="presentation"
                  ng-repeat="cat in vm.stack"
                  ng-class="{active:vm.selectedPanel === cat.code}"
                >
                  <a ng-click="vm.selectedPanel = cat.code">[[cat.category]]</a>
                </li>
              </ul>
            </div>
            <div class="col-sm-8">
              <div class="tab-content">
                <div
                  id="tab-[[cat.code]]"
                  class="tab-pane fade"
                  ng-class="{in: cat.code == vm.selectedPanel, active: cat.code == vm.selectedPanel}"
                  ng-repeat="cat in vm.stack"
                >
                  <h3>[[cat.category]]</h3>
                  <p>[[cat.description]]</p>
                  <div ng-repeat="dependency in cat.items">
                    <label ng-class="{'text-muted': dependency.disabled}">
                      <input
                        type="checkbox"
                        ng-disabled="dependency.disabled"
                        value="[[dependency.name]]"
                        ng-model="dependency.selected"
                        ng-change="$parent.vm.onDependencySelected($item, dependency, $label, $event)"
                      />
                      [[dependency.name]]
                    </label>
                    <p ng-class="{'text-muted': dependency.disabled}">[[dependency.description]]</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
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
              <i id="collapseAdvancedIcon" class="bi-plus-circle-fill" aria-hidden="true"></i>
            </button>
          </p>
        </div>
      </div>
      <div class="row collapse" id="collapseAdvanced">
        <div class="col-sm-12">
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
            :values="jdkVersions"
            prefix="JDK "
          />
          <hr />
        </div>
      </div>
      <div class="row" ng-if="store.project.vertxDependencies.length != 0">
        <div class="col-sm-12">
          <div>
            <label class="control-label"
              >Selected dependencies ([[store.project.vertxDependencies.length]])</label
            >
            <br />
            <div ng-repeat="dependency in vm.selectedDependencies">[[dependency.artifactId]]</div>

            <div class="tag" ng-repeat="dependency in store.project.vertxDependencies">
              [[ dependency.name ]]
              <button
                type="button"
                class="remove"
                aria-label="Remove"
                ng-click="vm.removeDependency(dependency.artifactId)"
              >
                <span aria-hidden="true">&times;</span>
              </button>
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
