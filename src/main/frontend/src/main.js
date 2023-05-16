import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'
import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'

fetch('/metadata')
  .then((r) => r.json())
  .then((metadata) => {
    createApp(App, { metadata: metadata }).mount('#app')
  })
