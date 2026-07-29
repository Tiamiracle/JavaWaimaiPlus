import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const store = new Vuex.Store({
	state: {
		storeInfo: {}, // 店铺请求id信息
		shopInfo: '',  // 店铺详细信息
		orderListData: [],// 购物车列表
		baseUserInfo: '', // 微信授权信息：微信昵称、微信头像、openid
		lodding: false,
		sessionId: '',
		addressBackUrl: '',
		dishTypeIndex: 0,
		shopPhone: '', //店铺电话
		shopStatus: {}, //店铺状态
		orderData: {},
		token: '',       // 鉴权必备
		userId: 0,       // 用户主键必备
		arrivals: '',
		remarkData: '',//备注
		addressData: {}, //地址选择
		deliveryFee: 0// 配送费
	},
	mutations: {
		setUserId(state, val) {
			state.userId = val
		},
		setStoreInfo(state, provider) {
			state.storeInfo = provider
		},
		setShopInfo(state, provider) {
			state.shopInfo = provider
		},
		initdishListMut(state, provider) {
			state.orderListData = provider
		},
		setBaseUserInfo(state, provider) {
			state.baseUserInfo = provider
		},
		setLodding(state, provider) {
			state.lodding = provider
		},
		setSessionId(state, provider) {
			state.sessionId = provider
		},
		setAddressBackUrl(state, provider) {
			state.addressBackUrl = provider
		},
		setDishTypeIndex(state, provider) {
			state.dishTypeIndex = provider
		},
		setShopPhone(state, provider) {
			state.shopPhone = provider
		},
		setShopStatus(state, provider) {
			state.shopStatus = provider
		},
		setOrderData(state, provider) {
			state.orderData = provider
		},
		setToken(state, provider) {
			state.token = provider
		},
		setArrivalTime(state, provider) {
			state.arrivals = provider
		},
		setRemark(state, provider) {
			state.remarkData = provider
		},
		setAddress(state, provider) {
			state.addressData = provider
		},
		setDeliveryFee(state, deliveryFee) {
			state.deliveryFee = deliveryFee
		}
	},
	actions: {

	}
})

export default store