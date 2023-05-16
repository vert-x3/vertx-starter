import { reactive } from 'vue'

export const store = reactive({
  project: {
    groupId: '',
    artifactId: '',
    language: '',
    buildTool: '',
    vertxVersion: '',
    archiveFormat: '',
    vertxDependencies: [],
    packageName: '',
    jdkVersion: ''
  }
})
