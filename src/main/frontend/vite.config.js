import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '~bootstrap': fileURLToPath(new URL('node_modules/bootstrap', import.meta.url)),
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler',
        quietDeps: true
      }
    }
  },
  server: {
    proxy: {
      '/metadata': {
        target: 'http://localhost:8080'
      },
      '/starter.zip': {
        target: 'http://localhost:8080'
      }
    }
  }
})
