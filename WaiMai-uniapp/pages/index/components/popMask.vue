<!--选择多规格弹层-->
<template>
  <view class="more_norm_pop" @click.stop>
    <view class="title">{{ moreNormDishdata.name }}</view>
    <scroll-view class="items_cont" scroll-y="true" scroll-top="0rpx">
      <view class="item_row" v-for="(obj, index) in moreNormdata" :key="index">
        <view class="flavor_name">{{ obj.name }}</view>
        <view class="flavor_item">
          <view
            :class="{
              item: true,
              act: flavorDataes.findIndex((it) => item === it) !== -1,
            }"
            v-for="(item, ind) in obj.value"
            :key="ind"
            @tap="checkMoreNormPop(obj.value, item)"
          >
            {{ item }}
          </view>
        </view>
      </view>
    </scroll-view>
    <view class="but_item">
      <view class="price">
        <text class="ico">￥</text>
        {{ moreNormDishdata.price }}
      </view>
      <view class="active"
        ><view class="dish_card_add" @tap="addShop(moreNormDishdata)"
          >加入购物车</view
        ></view
      >
    </view>
    <view class="close" @tap="closeMoreNorm(moreNormDishdata)"
      ><image
        class="close_img"
        src="../../../static/but_close.png"
        mode=""
      ></image
    ></view>
  </view>
</template>
<script>
export default {
  // 获取父级传的数据
  props: {
    // 空页面提示
    moreNormDishdata: {
      type: Object,
      default: () => ({}),
    },
    moreNormdata: {
      type: Array,
      default: () => [],
    },
    flavorDataes: {
      type: Array,
      default: () => [],
    },
  },
  methods: {
    checkMoreNormPop(obj, item) {
      this.$emit("checkMoreNormPop", { obj: obj, item: item });
    },
    addShop(obj) {
      console.log(obj);
      this.$emit("addShop", obj);
    },
    closeMoreNorm(obj) {
      this.$emit("closeMoreNorm", obj);
    },
  },
};
</script>
<style lang="scss" scoped>
.more_norm_pop {
  width: calc(100vw - 120rpx);
  max-height: 80vh;
  box-sizing: border-box;
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translateX(-50%) translateY(-50%);
  background: #fff;
  border-radius: 24rpx;
  z-index: 10000;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  .div_big_image {
    width: 100%;
    border-radius: 10rpx;
  }
  .title {
    font-size: 36rpx;
    line-height: 60rpx;
    text-align: left;
    font-weight: 700;
    color: #1a1a1a;
    padding: 32rpx 32rpx 16rpx;
  }
  .items_cont {
    flex: 1;
    min-height: 0;
    display: block;
    padding: 0 32rpx;
    box-sizing: border-box;
    .item_row {
      display: flex;
      flex-direction: column;
      padding-bottom: 20rpx;
      .flavor_name {
        height: 40rpx;
        opacity: 1;
        font-size: 28rpx;
        font-family: PingFangSC, PingFangSC-Regular;
        font-weight: 400;
        text-align: left;
        color: #666666;
        line-height: 40rpx;
        padding-left: 10rpx;
        padding-top: 20rpx;
      }
      .flavor_item {
        display: flex;
        flex-wrap: wrap;
        .item {
          border: 1px solid #ffb302;
          border-radius: 12rpx;
          margin: 20rpx 10rpx;
          padding: 0 26rpx;
          height: 60rpx;
          line-height: 60rpx;
          font-family: PingFangSC, PingFangSC-Regular;
          font-weight: 400;
          color: #333333;
        }
        .act {
          // background: linear-gradient(144deg, #ffda05 18%, #ffb302 80%);
          background: #ffc200;
          border: 1px solid #ffc200;
          font-family: PingFangSC, PingFangSC-Medium;
          font-weight: 500;
        }
      }
    }
  }
  .but_item {
    display: flex;
    position: relative;
    align-items: center;
    padding: 20rpx 32rpx;
    border-top: 1rpx solid #f0f0f0;
    flex-shrink: 0;
    .price {
      flex: 1;
      text-align: left;
      color: #e94e3c;
      font-size: 44rpx;
      font-family: DIN, DIN-Medium;
      font-weight: 700;
      .ico {
        font-size: 26rpx;
      }
    }
    .active {
      position: relative;
      display: flex;
      align-items: center;
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