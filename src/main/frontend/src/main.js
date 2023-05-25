import 'bootstrap-icons/font/bootstrap-icons.scss'
import './assets/main.scss'

import { createApp } from 'vue'
import App from './App.vue'
import { store } from '@/store'

fetch('/metadata')
  .then((response) => response.json())
  .then((json) => {
    store.initialize(json)
    createApp(App).mount('#app')
  })
