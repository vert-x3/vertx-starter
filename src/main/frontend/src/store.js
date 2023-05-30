import { computed, reactive, ref } from 'vue'

export const store = {
  project: reactive({
    groupId: '',
    artifactId: '',
    language: '',
    buildTool: '',
    vertxVersion: '',
    archiveFormat: '',
    vertxDependencies: [],
    packageName: '',
    jdkVersion: ''
  }),
  projectDefaults: {
    vertxDependencies: [],
    packageName: ''
  },
  stack: [],
  buildTools: [],
  languages: [],
  jdkVersions: [],
  vertxVersions: [],
  excludedModulesByVersion: {},
  availableVertxDependencies: computed(() => {
    const excludedOfVersion = store.excludedModulesByVersion[store.project.vertxVersion]
    const selectedDependencies = store.project.vertxDependencies
    const res = []
    for (const category of store.stack) {
      for (const item of category.items) {
        if (!(excludedOfVersion.includes(item.artifactId) || selectedDependencies.includes(item))) {
          res.push(item)
        }
      }
    }
    return res
  }),
  invalidFields: ref(new Set()),
  isGenerating: ref(false),
  alerts: reactive([]),
  initialize(json) {
    Object.assign(this.projectDefaults, json.defaults)
    for (const prop of ['stack', 'buildTools', 'languages', 'jdkVersions']) {
      this[prop] = json[prop]
    }
    for (const version of json.versions) {
      this.vertxVersions.push(version.number)
      this.excludedModulesByVersion[version.number] = version.exclusions
    }
    this.reset()
  },
  reset() {
    Object.assign(this.project, this.projectDefaults)
    this.onVersionChanged()
  },
  resetAdvanced() {
    this.project.packageName = this.projectDefaults.packageName
    this.project.jdkVersion = this.projectDefaults.jdkVersion
  },
  onVersionChanged() {
    const excludedOfVersion = store.excludedModulesByVersion[store.project.vertxVersion]
    for (const category of store.stack) {
      for (const item of category.items) {
        if (excludedOfVersion.includes(item.artifactId)) {
          item.disabled = true
          item.selected = false
        } else {
          item.disabled = false
        }
      }
    }
    const idx = store.project.vertxDependencies.findIndex((dep) => {
      return excludedOfVersion.includes(dep.artifactId)
    })
    if (idx >= 0) {
      store.project.vertxDependencies.splice(idx, 1)
    }
  },
  addDependency(dependency) {
    const idx = store.project.vertxDependencies.findIndex(
      (dep) => dep.artifactId === dependency.artifactId
    )
    if (idx < 0) {
      store.project.vertxDependencies.push(dependency)
    }
  },
  removeDependency(dependency) {
    const idx = store.project.vertxDependencies.findIndex(
      (dep) => dep.artifactId === dependency.artifactId
    )
    if (idx >= 0) {
      store.project.vertxDependencies.splice(idx, 1)
    }
  }
}
