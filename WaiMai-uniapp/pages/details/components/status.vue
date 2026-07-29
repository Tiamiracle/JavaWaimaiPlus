<!-- 订单状态 -->
<template>
	<view>
		<view class="box">
			<view class="orderInfoTip">
				<!-- 去掉入参，直接调用无参statusWord -->
				<view class="tit">{{ statusWord() }}</view>
				<view class="rejectionReason" v-if="orderDetailsData.status === 7">
					<text v-if="orderDetailsData.payStatus === 1 || orderDetailsData.payStatus === 2">退款成功</text>
					<text v-else-if="orderDetailsData.cancelReason">{{ orderDetailsData.cancelReason }}</text>
					<text v-else-if="orderDetailsData.rejectionReason">{{ orderDetailsData.rejectionReason }}</text>
				</view>
				<!-- 倒计时区域改用 realStatus 判断 -->
				<view v-if="realStatus === 1">
					<view class="time">
						<view class="timeIcon"></view>
						等待支付：
						<text>{{ rocallTime }}</text>
					</view>
				</view>
				<view class="againBtn">
					<!-- 取消按钮：仅1/2/3/4状态显示 -->
					<button class="new_btn" type="default" @click="handleCancel('center', orderDetailsData)" 
						v-if="[1,2,3,4].includes(realStatus)">
						取消订单
					</button>
					<!-- 立即支付：仅待付款显示 -->
					<button class="new_btn btn" type="default" @click="handlePay(orderDetailsData.id)"
						v-if="realStatus === 1">
						立即支付
					</button>
					<button class="new_btn btn" type="default" @click="handleReminder('center', orderDetailsData.id)"
						v-if="orderDetailsData.status === 2">
						催单
					</button>
					<button class="new_btn" type="default" @click="handleRefund('center')"
						v-if="orderDetailsData.status == 5">
						申请退款
					</button>
					<button class="new_btn" type="default" @click="oneMoreOrder(orderDetailsData.id)"
						v-if="orderDetailsData.status !== 7">
						再来一单
					</button>
				</view>
			</view>
		</view>
		<!-- 15分钟支付提示 -->
		<view class="box timeTip" v-if="realStatus === 1">
			<view class="icon newIcon"></view>
			请在15分钟内完成支付，超时将自动取消。
		</view>
		<!-- 退款成功提示，兼容前端timeout和后端status=6 -->
		<view class="box timeTip" v-if="(realStatus === -1 || orderDetailsData.status === 6) && orderDetailsData.payStatus === 2">
			<view class="icon moneyIcon"></view>
			您的订单已
			<text>退款成功</text>
			。
		</view>
	</view>
</template>
<script>
	import { statusWord } from "@/utils/index";
	export default {
		props: {
			orderDetailsData: {
				type: Object,
				default: () => ({}),
			},
			timeout: {
				type: Boolean,
				default: false,
			},
			rocallTime: {
				type: String,
				default: "",
			},
		},
		computed: {
			realStatus() {
				if (this.timeout) return -1 // 前端超时取消
				return this.orderDetailsData.status
			}
		},
		methods: {
			statusWord() {
				const s = this.realStatus
				if (s === -1 || s === 6) {
					return "订单已取消"
				}
				return statusWord(s);
			},
			handleCancel(type, obj) {
				this.$emit("handleCancel", { type: type, obj: obj });
			},
			handlePay(id) {
				this.$emit("handlePay", id);
			},
			handleReminder(type, id) {
				this.$emit("handleReminder", { type: type, id: id });
			},
			handleRefund(type) {
				this.$emit("handleRefund", type);
			},
			oneMoreOrder(id) {
				this.$emit("oneMoreOrder", id);
			},
		},
	};
</script>
<style src="../../order/style.scss" lang="scss"></style>