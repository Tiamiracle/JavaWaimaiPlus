<template>
  <!-- 智能助手悬浮按钮 -->
  <view v-if="isShopOpen && showBtn" class="float-btn" @tap="handleClick">
    <image class="btn-icon" src="/static/机器人.png" mode="aspectFit" />
  </view>
</template>

<script>
import { mapState } from 'vuex'
export default {
  name: "FloatAssistant",
  props: {
    // 店铺营业状态，由外层page‑wrapper从vuex传入
    isShopOpen: {
      type: Boolean,
      default: true
    },
    // 是否显示按钮（用于在特定页面隐藏，例如聊天页）
    showBtn: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    ...mapState(['token'])
  },
  methods: {
    handleClick() {
      if (!this.token) {
        uni.showModal({
          title: "提示",
          content: "请登录后使用智能助手",
          confirmText: "去登录",
          cancelText: "取消",
          success: (res) => {
            if (res.confirm) {
              this.$emit('need-login')
            }
          }
        })
        return
      }
      uni.navigateTo({ url: "/pages/chat/chat" })
    }
  }
}
</script>

<style lang="scss" scoped>
.float-btn {
  position: fixed;
  right: 32rpx;
  bottom: 180rpx;
  width: 112rpx;
  height: 112rpx;
  background: linear-gradient(145deg, #ffd54f, #ffc200);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 12rpx rgba(255, 194, 0, 0.28), 0 12rpx 30rpx rgba(255, 194, 0, 0.32);
  z-index: 9999;
  transition: all 0.2s ease;
  animation: breath 3s ease-in-out infinite;

  &:active {
    transform: scale(0.88);
    opacity: 0.85;
    box-shadow: 0 2rpx 8rpx rgba(255, 194, 0, 0.2);
  }

  .btn-icon {
    width: 68rpx;
    height: 68rpx;
  }
}

@keyframes breath {
  0% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.06);
  }

  100% {
    transform: scale(1);
  }
}
</style>