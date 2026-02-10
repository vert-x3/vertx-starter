import { includeIgnoreFile } from '@eslint/compat'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

import pluginVue from 'eslint-plugin-vue'
import vuePrettierConfig from '@vue/eslint-config-prettier/skip-formatting'
import js from '@eslint/js'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const gitignorePath = path.resolve(__dirname, '.gitignore')

export default [
  {
    name: 'app/files-to-lint',
    files: ['**/*.{vue,js,jsx,cjs,mjs}']
  },

  includeIgnoreFile(gitignorePath),

  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  vuePrettierConfig,

  {
    name: 'app/language-options',
    languageOptions: {
      ecmaVersion: 'latest'
    }
  }
]
