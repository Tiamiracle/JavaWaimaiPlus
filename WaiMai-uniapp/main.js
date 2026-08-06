import Vue from 'vue'
import App from './App'
import store from './store'
import "@/styles/common.scss"
// 引入全局组件
import chatAi from '@/components/chatAi.vue'

// 1
Vue.config.productionTip = false
Vue.prototype.$store = store

// 全局注册组件
Vue.component('chat-ai', chatAi)

App.mpType = 'app'

const app = new Vue({
	store,
    ...App
})
app.$mount()
