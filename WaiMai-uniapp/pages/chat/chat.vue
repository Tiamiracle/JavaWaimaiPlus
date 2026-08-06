<template>
	<view class="chat-page">
		<!-- 顶部自定义导航栏 -->
		<view class="nav-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="nav-content">
				<view class="nav-back" @tap="goBack">
					<text class="back-icon">‹</text>
				</view>
				<view class="nav-title">
					<image class="nav-avatar" src="/static/机器人.png" mode="aspectFit" />
					<text class="nav-title-text">智能助手 · 小苍</text>
				</view>
				<view class="nav-clear" @tap="handleClear">
					<text class="clear-text">清空</text>
				</view>
			</view>
		</view>

		<!-- 占位，防止内容被导航栏遮挡 -->
		<view :style="{ height: (statusBarHeight + 44) + 'px' }"></view>

		<!-- 消息列表区域 -->
		<scroll-view scroll-y class="msg-scroll" :scroll-into-view="scrollIntoView" :scroll-with-animation="true"
			:style="{ height: 'calc(100vh - ' + (statusBarHeight + 44) + 'px - 200rpx)' }">
			<view class="msg-list">
				<!-- 遍历消息列表 -->
				<view v-for="(item, index) in msgList" :key="index" :id="'msg-' + index" class="msg-item"
					:class="item.role === 'user' ? 'msg-user' : 'msg-ai'">
					<!-- AI 头像 -->
					<view v-if="item.role !== 'user'" class="ai-avatar">
						<image src="/static/机器人.png" mode="aspectFit" class="avatar-img" />
					</view>

					<!-- 气泡 -->
					<view class="bubble" :class="item.role === 'user' ? 'bubble-user' : 'bubble-ai'">
						<text class="bubble-text">{{ item.content }}</text>
					</view>
				</view>

				<!-- AI 正在输入占位 -->
				<view v-if="isLoading" class="msg-item msg-ai" id="msg-loading">
					<view class="ai-avatar">
						<image src="/static/机器人.png" mode="aspectFit" class="avatar-img" />
					</view>
					<view class="bubble bubble-ai bubble-loading">
						<view class="loading-dots">
							<view class="dot dot1"></view>
							<view class="dot dot2"></view>
							<view class="dot dot3"></view>
						</view>
					</view>
				</view>

				<!-- 底部留白 -->
				<view class="scroll-bottom-pad"></view>
			</view>
		</scroll-view>

		<!-- 快捷问题标签 -->
		<scroll-view scroll-x class="quick-scroll" show-scrollbar="false">
			<view class="quick-tags">
				<view v-for="(t, i) in quickList" :key="i" class="tag" @tap="sendQuick(t)">
					<text class="tag-text">{{ t }}</text>
				</view>
			</view>
		</scroll-view>

		<!-- 底部输入框区域 -->
		<view class="input-area" :style="{ paddingBottom: safeBottom + 'px' }">
			<view class="input-wrap">
				<input v-model="inputText" placeholder="请输入你的问题..." placeholder-class="input-placeholder" class="input"
					confirm-type="send" @confirm="handleSend" />
			</view>
			<view class="send-btn" :class="{ 'send-btn-active': inputText.trim() }" @tap="handleSend">
				<text class="send-text">发送</text>
			</view>
		</view>
	</view>
</template>

<script>
import { request } from "@/utils/request.js"

// 聊天相关接口
const chatApi = {
	getHistory() {
		return request({ url: "/user/chat/history", method: "GET" })
	},
	sendMessage(params) {
		return request({ url: "/user/chat/send", method: "POST", params })
	},
	clearHistory() {
		return request({ url: "/user/chat/history", method: "DELETE" })
	}
}

export default {
	data() {
		return {
			statusBarHeight: 20,
			safeBottom: 0,
			msgList: [],//历史聊天记录列表
			scrollIntoView: "",//聊天自动滚动
			isLoading: false,//是否加载
			inputText: "",//输入框内容
			quickList: ["配送多久到达", "如何申请退款", "营业时间"]//快捷问题标签
		}
	},
	onLoad() {
		// 获取状态栏高度
		const sysInfo = uni.getSystemInfoSync()
		this.statusBarHeight = sysInfo.statusBarHeight || 20
		// 适配 iPhone 底部安全区域
		this.safeBottom = sysInfo.safeAreaInsets ? sysInfo.safeAreaInsets.bottom : 0
		this.fetchHistory()
	},
	methods: {
		// 返回上一页
		goBack() {
			uni.navigateBack({ delta: 1 })
		},

		// 拉取后端历史会话
		async fetchHistory() {
			const res = await chatApi.getHistory()
			const list = res.data || []
			if (list.length === 0) {
				// 无历史记录
				this.msgList.push({
					role: "assistant",
					content: "你好，我是小苍，苍穹外卖智能助手，可以帮你查询菜品、套餐、配送相关问题。"
				})
			} else {
				// 有历史记录
				this.msgList = list
			}
			this.scrollBottom()
		},

		// 点击快捷标签发送
		sendQuick(text) {
			this.inputText = text
			this.handleSend()
		},

		// 发送消息主逻辑
		async handleSend() {
			const content = this.inputText.trim()
			if (!content || this.isLoading) return

			// 1. 前端立刻渲染用户消息
			this.msgList.push({ role: "user", content })
			this.inputText = ""
			this.isLoading = true
			this.scrollBottom()
			console.log('用户发送信息', content)
			// 2. 调用后端发送接口拿到ai回复
			const res = await chatApi.sendMessage({content:content})
			this.msgList.push({
				role: "assistant",
				content: (res.data && res.data.content) || "抱歉，我没有理解你的问题，请换个方式提问。"
			})
			// ✅ 无论成功失败，关闭loading
			this.isLoading = false
			this.scrollBottom()

		},

		// 清空会话
		// async handleClear() {
		// 	uni.showModal({
		// 		title: "提示",
		// 		content: "确定要清空所有聊天记录吗？",
		// 		confirmText: "清空",
		// 		confirmColor: "#ff5a2a",
		// 		success: async (res) => {
		// 			if (!res.confirm) return
		// 			try {
		// 				await chatApi.clearHistory()
		// 				this.msgList = [{
		// 					role: "assistant",
		// 					content: "会话已清空，有什么可以帮你的吗？"
		// 				}]
		// 				this.scrollBottom()
		// 				uni.showToast({ title: "已清空", icon: "success" })
		// 			} catch (e) {
		// 				// 接口失败也清空本地
		// 				this.msgList = [{
		// 					role: "assistant",
		// 					content: "会话已清空，有什么可以帮你的吗？"
		// 				}]
		// 				this.scrollBottom()
		// 			}
		// 		}
		// 	})
		// },

		// 滚动到底部
		scrollBottom() {
			this.$nextTick(() => {
				const last = this.isLoading ? "msg-loading" : "msg-" + (this.msgList.length - 1)
				this.scrollIntoView = ""
				setTimeout(() => {
					this.scrollIntoView = last
				}, 50)
			})
		}
	}
}
</script>

<style lang="scss" scoped>
.chat-page {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background: #f5f5f7;
}

/* ===== 导航栏 ===== */
.nav-bar {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 100;
	background: linear-gradient(135deg, #ffd54f 0%, #ffc200 100%);
	box-shadow: 0 4rpx 16rpx rgba(255, 194, 0, 0.3);
}

.nav-content {
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 24rpx;
}

.nav-back {
	width: 70rpx;
	height: 70rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.back-icon {
	font-size: 56rpx;
	color: #333333;
	font-weight: 300;
	line-height: 1;
	margin-top: -8rpx;
}

.nav-title {
	display: flex;
	align-items: center;
}

.nav-avatar {
	width: 48rpx;
	height: 48rpx;
	margin-right: 12rpx;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.2);
}

.nav-title-text {
	font-size: 32rpx;
	font-weight: 600;
	color: #333333;
}

.nav-clear {
	width: 80rpx;
	height: 56rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.clear-text {
	font-size: 26rpx;
	color: rgba(51, 51, 51, 0.8);
}

/* ===== 消息列表 ===== */
.msg-scroll {
	flex: 1;
}

.msg-list {
	padding: 24rpx 24rpx 0;
}

.msg-item {
	margin-bottom: 32rpx;
	display: flex;
	align-items: flex-start;
}

.msg-user {
	flex-direction: row-reverse;
}

/* AI 头像 */
.ai-avatar {
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #ffd54f 0%, #ffc200 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
	margin-right: 16rpx;
	box-shadow: 0 4rpx 12rpx rgba(255, 194, 0, 0.3);
}

.avatar-img {
	width: 48rpx;
	height: 48rpx;
}

/* 气泡 */
.bubble {
	max-width: 480rpx;
	padding: 20rpx 24rpx;
	border-radius: 20rpx;
	word-break: break-all;
}

.bubble-ai {
	background: #ffffff;
	border-top-left-radius: 6rpx;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.bubble-user {
	background: linear-gradient(135deg, #ffd54f 0%, #ffc200 100%);
	border-top-right-radius: 6rpx;
	box-shadow: 0 4rpx 16rpx rgba(255, 194, 0, 0.3);
}

.bubble-text {
	font-size: 28rpx;
	line-height: 1.6;
}

.bubble-ai .bubble-text {
	color: #333333;
}

.bubble-user .bubble-text {
	color: #333333;
}

/* 加载动画 */
.bubble-loading {
	padding: 24rpx 28rpx;
}

.loading-dots {
	display: flex;
	align-items: center;
}

.dot {
	width: 12rpx;
	height: 12rpx;
	border-radius: 50%;
	background: #999;
	margin-right: 8rpx;
	animation: dotBounce 1.4s infinite ease-in-out;
}

.dot:last-child {
	margin-right: 0;
}

.dot1 {
	animation-delay: 0s;
}

.dot2 {
	animation-delay: 0.2s;
}

.dot3 {
	animation-delay: 0.4s;
}

@keyframes dotBounce {

	0%,
	80%,
	100% {
		transform: scale(0.6);
		opacity: 0.4;
	}

	40% {
		transform: scale(1);
		opacity: 1;
	}
}

.scroll-bottom-pad {
	height: 20rpx;
}

/* ===== 快捷标签 ===== */
.quick-scroll {
	flex-shrink: 0;
	white-space: nowrap;
	background: #f5f5f7;
}

.quick-tags {
	display: flex;
	padding: 15rpx;
	gap: 5rpx;
}

.tag {
	flex: 1;
	align-items: center;
	padding: 12rpx 28rpx;
	background: #ffffff;
	border-radius: 32rpx;
	border: 1rpx solid #eee;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.04);
}

.tag:active {
	background: #f0f0f5;
}

.tag-text {
	font-size: 26rpx;
	color: #ff9500;
}

/* ===== 输入区域 ===== */
.input-area {
	flex-shrink: 0;
	display: flex;
	align-items: center;
	padding: 16rpx 24rpx;
	background: #ffffff;
	border-top: 1rpx solid #eee;
}

.input-wrap {
	flex: 1;
	background: #f5f5f7;
	border-radius: 36rpx;
	padding: 0 28rpx;
	margin-right: 16rpx;
}

.input {
	height: 72rpx;
	font-size: 28rpx;
	color: #333;
}

.input-placeholder {
	color: #999;
	font-size: 28rpx;
}

.send-btn {
	width: 120rpx;
	height: 72rpx;
	border-radius: 36rpx;
	background: #ddd;
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.2s ease;
}

.send-btn-active {
	background: linear-gradient(135deg, #ffd54f 0%, #ffc200 100%);
	box-shadow: 0 4rpx 12rpx rgba(255, 194, 0, 0.3);
}

.send-btn-active:active {
	transform: scale(0.95);
}

.send-text {
	font-size: 28rpx;
	font-weight: 600;
	color: #333333;
}
</style>
