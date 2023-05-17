import 'bootstrap-icons/font/bootstrap-icons.scss'
import './assets/main.scss'

import 'bootstrap'

import { createApp } from 'vue'
import App from './App.vue'

fetch('/metadata')
  .then((r) => r.json())
  .then((metadata) => {
    createApp(App, { metadata: metadata }).mount('#app')
  })
