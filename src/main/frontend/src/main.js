import '@fontsource/ubuntu/500.css'
import 'bootstrap-icons/font/bootstrap-icons.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import './assets/stylesheets/main.css'
import './assets/main.css'

import 'bootstrap'

import { createApp } from 'vue'
import App from './App.vue'

fetch('/metadata')
  .then((r) => r.json())
  .then((metadata) => {
    createApp(App, { metadata: metadata }).mount('#app')
  })
