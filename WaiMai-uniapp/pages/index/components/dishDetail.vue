<!--选择多规格弹层-->
<template>
  <!-- 餐品详情 -->
  <view class="dish_detail_pop" v-if="dishDetailes.type == 1">
    <scroll-view class="dish_detail_scroll" scroll-y>
      <image
        mode="aspectFill"
        class="div_big_image"
        :src="dishDetailes.image"
      ></image>
      <view class="title">{{ dishDetailes.name }}</view>
      <view class="desc" v-if="dishDetailes.description">{{ dishDetailes.description }}</view>
      <view class="reason-box" v-if="dishDetailes.reason">
        <text class="reason-label">💡 推荐理由</text>
        <text class="reason-content">{{ dishDetailes.reason }}</text>
      </view>
      <view class="sales-row">月售 {{ dishDetailes.sales || 0 }}</view>
    </scroll-view>
    <view class="but_item">
      <view class="price">
        <text class="ico">￥</text>
        {{ Number(dishDetailes.price).toFixed(2) }}
      </view>
      <view
        class="active"
        v-if="dishDetailes.flavors.length === 0 && dishDetailes.dishNumber > 0"
      >
        <image
          src="../../../static/btn_red.png"
          @tap="redDishAction(dishDetailes, '普通')"
          class="dish_red"
          mode=""
        ></image>
        <text class="dish_number">{{ dishDetailes.dishNumber }}</text>
        <image
          src="../../../static/btn_add.png"
          class="dish_add"
          @tap="addDishAction(dishDetailes, '普通')"
          mode=""
        ></image>
      </view>

      <view class="active" v-if="dishDetailes.flavors.length > 0"
        ><view class="dish_card_add" @tap="moreNormDataesHandle(dishDetailes)"
          >选择规格</view
        ></view
      >
      <view
        class="active"
        v-if="
          dishDetailes.dishNumber === 0 && dishDetailes.flavors.length === 0
        "
      >
        <view class="dish_card_add" @tap="addDishAction(dishDetailes, '普通')"
          >加入购物车</view
        >
      </view>
    </view>
    <view class="close" @tap="dishClose"
      ><image
        class="close_img"
        src="../../../static/but_close.png"
        mode=""
      ></image
    ></view>
  </view>
  <!-- end -->
  <!-- 套餐详情 -->
  <view class="dish_detail_pop" v-else>
    <scroll-view class="dish_items" scroll-y="true" scroll-top="0rpx">
      <view
        class="dish_item"
        v-for="(item, index) in dishMealData"
        :key="index"
      >
        <image class="div_big_image" :src="item.image" mode=""></image>
        <view class="title">
          {{ item.name }}
          <text style="">X{{ item.copies }}</text>
        </view>
        <view class="desc">{{ item.description }}</view>
      </view>
    </scroll-view>
    <view class="but_item">
      <view class="price">
        <text class="ico">￥</text>
        {{ dishDetailes.price }}
      </view>
      <view
        class="active"
        v-if="dishDetailes.dishNumber && dishDetailes.dishNumber > 0"
      >
        <image
          src="../../../static/btn_red.png"
          @tap="redDishAction(dishDetailes, '普通')"
          class="dish_red"
          mode=""
        ></image>
        <text class="dish_number">{{ dishDetailes.dishNumber }}</text>
        <image
          src="../../../static/btn_add.png"
          class="dish_add"
          @tap="addDishAction(dishDetailes, '普通')"
          mode=""
        ></image>
      </view>
      <view class="active" v-else-if="dishDetailes.dishNumber == 0"
        ><view
          class="dish_card_add"
          @tap="addDishAction(dishDetailes, '普通')"
          >加入购物车</view
        ></view
      >
    </view>
    <view class="close" @tap="dishClose"
      ><image
        class="close_img"
        src="../../../static/but_close.png"
        mode=""
      ></image
    ></view>
  </view>
  <!-- end -->
</template>
<script>
export default {
  // 获取父级传的数据
  props: {
    dishDetailes: {
      type: Object,
      default: () => ({}),
    },
    openDetailPop: {
      type: Boolean,
      default: false,
    },
    dishMealData: {
      type: Array,
      default: () => [],
    },
  },
  methods: {
    // 加入购物车
    addDishAction(obj, item) {
      this.$emit("addDishAction", obj, item);
    },
    redDishAction(obj, item) {
      this.$emit("redDishAction", obj, item);
    },
    // 选择规格
    moreNormDataesHandle(obj) {
      this.$emit("moreNormDataesHandle", obj);
    },
    // 关闭菜单详情
    dishClose() {
      this.$emit("dishClose");
    },
  },
};
</script>
<style lang="scss" scoped>
.dish_detail_pop {
  width: calc(100vw - 120rpx);
  max-height: 80vh;
  box-sizing: border-box;
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translateX(-50%) translateY(-50%);
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  z-index: 10001;

  .dish_detail_scroll {
    flex: 1;
    min-height: 0;
    padding: 32rpx 32rpx 16rpx;
  }

  .div_big_image {
    width: 100%;
    height: 320rpx;
    border-radius: 16rpx;
    display: block;
  }

  .title {
    font-size: 36rpx;
    line-height: 60rpx;
    text-align: left;
    font-weight: 700;
    color: #1a1a1a;
    margin-top: 20rpx;
  }

  .desc {
    font-size: 26rpx;
    color: #666;
    line-height: 1.6;
    margin-top: 12rpx;
  }

  .reason-box {
    display: flex;
    flex-direction: column;
    margin-top: 16rpx;
    padding: 16rpx 20rpx;
    background: linear-gradient(90deg, #fff4ec 0%, #fff8f2 100%);
    border-radius: 12rpx;
  }

  .reason-label {
    font-size: 24rpx;
    font-weight: 600;
    color: #ff5a2a;
    margin-bottom: 6rpx;
  }

  .reason-content {
    font-size: 24rpx;
    color: #888;
    line-height: 1.5;
  }

  .sales-row {
    font-size: 22rpx;
    color: #999;
    margin-top: 12rpx;
  }

  .dish_items {
    flex: 1;
    min-height: 0;
    padding: 0 32rpx;
  }

  .but_item {
    display: flex;
    position: relative;
    align-items: center;
    padding: 20rpx 32rpx;
    border-top: 1rpx solid #f0f0f0;
    background: #fff;
    flex-shrink: 0;
    .price {
      flex: 1;
      text-align: left;
      color: #e94e3c;
      font-size: 44rpx;
      font-weight: 700;
      .ico {
        font-size: 26rpx;
      }
    }
    .active {
      display: flex;
      align-items: center;
      position: relative;
      right: auto;
      bottom: auto;
      .dish_add,
      .dish_red {
        display: block;
        width: 64rpx;
        height: 64rpx;
      }
      .dish_number {
        padding: 0 12rpx;
        line-height: 64rpx;
        font-size: 30rpx;
        font-family: PingFangSC, PingFangSC-Medium;
        font-weight: 500;
        color: #333;
      }
      .dish_card_add {
        width: 200rpx;
        height: 72rpx;
        line-height: 72rpx;
        text-align: center;
        font-weight: 600;
        font-size: 28rpx;
        color: #333;
        background: linear-gradient(135deg, #ffc200 0%, #ffb302 100%);
        border-radius: 36rpx;
        box-shadow: 0 4rpx 12rpx rgba(255, 194, 0, 0.35);
      }
    }
  }
}
.close {
  position: absolute;
  bottom: -120rpx;
  left: 50%;
  transform: translateX(-50%);
  .close_img {
    width: 80rpx;
    height: 80rpx;
  }
}
</style>