<template>
  <view>
    <!-- 导航 -->
    <navBar></navBar>
    <!-- end -->
    <view class="home_content" :style="{ paddingTop: ht + 'px' }" @touchmove.stop.prevent="disabledScroll">
      <!-- 店铺基本信息 -->
      <view class="restaurant_info_box">
        <view class="restaurant_info">
          <!-- 上部 -->
          <view class="info_top">
            <view class="info_top_left">
              <image class="logo_ruiji" src="../../static/logo_ruiji.png"></image>
            </view>
            <view class="info_top_right">
              <view class="right_title">
                <text>苍穹外卖</text>
                <view class="businessStatus" v-if="shopStatus === 1">营业中</view>
                <view class="businessStatus close" v-else>休息中</view>
              </view>
              <view class="right_details">
                <!-- 中 -->
                <view class="details_flex">
                  <image class="top_icon" src="../../static/money.png"></image>
                  <text class="icon_text">配送费{{ deliveryFee }}元</text>
                </view>
              </view>
            </view>
          </view>
          <!-- 下部---信息简介 -->
          <view class="info_bottom">
            <view>
              <view class="word">苍穹餐厅为顾客打造专业的大众化美食外送餐饮</view>
              <view class="address">
                <icon></icon>
                {{ shopInfo.shopAddress || "商家店铺获取中.." }}
              </view>
            </view>
            <view>
              <view class="phone" @click="handlePhone('bottom')">
                <icon class="phoneIcon"></icon>
              </view>
            </view>
          </view>
        </view>
      </view>
      <view class="recommend-section" v-if="recommendList.length > 0 && shopStatus === 1">
        <view class="recommend-header">
          <view class="recommend-title">
            <text class="title-icon">🔥</text>
            <text class="title-text">人气推荐</text>
            <text class="title-sub">根据你的口味精选</text>
          </view>
          <view class="recommend-more">每日精选</view>
        </view>
        <scroll-view class="recommend-scroll" scroll-x show-scrollbar="false">
          <view class="recommend-list">
            <view class="recommend-item" v-for="dish in recommendList" :key="dish.id" @click="openDetailHandle(dish)">
              <view class="recommend-img-wrap">
                <image class="recommend-img" :src="dish.image" mode="aspectFill" />
              </view>
              <view class="recommend-info">
                <text class="recommend-name">{{ dish.name }}</text>
                <view class="recommend-sales-row">
                  <text class="recommend-sales">月售 {{ dish.sales || 0 }}</text>
                </view>
                <view class="recommend-reason" v-if="dish.reason">
                  <text class="reason-icon">💡</text>
                  <text class="reason-text">{{ dish.reason }}</text>
                </view>
                <view class="recommend-bottom">
                  <view class="recommend-price-wrap">
                    <text class="recommend-price-symbol">¥</text>
                    <text class="recommend-price">{{ Number(dish.price).toFixed(2) }}</text>
                  </view>
                  <view class="recommend-add" @click.stop="addDishAction(dish, '普通')">
                    <text class="add-icon">+</text>
                  </view>
                </view>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
      <!-- end -->
      <!-- 菜单列表 -->
      <view class="restaurant_menu_list" v-if="shopStatus === 1">
        <view class="type_list">
          <scroll-view scroll-y scroll-with-animation class="u-tab-view menu-scroll-view" :scroll-top="scrollTop + 100"
            :scroll-into-view="itemId">
            <view class="type_item" id="target" :class="[typeIndex == index ? 'active' : '']"
              v-for="(item, index) in typeListData" :key="index" @tap.stop="swichMenu(item, index)">
              <view class="item" :class="item.name.length > 5 ? 'allLine' : ''">{{ item.name }}</view>
            </view>
            <view class="seize_seat"></view>
          </scroll-view>
        </view>
        <scroll-view class="vegetable_order_list" scroll-y="true" scroll-top="0rpx"
          v-if="dishListItems && dishListItems.length > 0">
          <view class="type_item" v-for="(item, index) in dishListItems" :key="index">
            <!-- 点击查看详情 -->
            <view class="dish_img" @click="openDetailHandle(item)">
              <image mode="aspectFill" :src="item.image" class="dish_img_url"></image>
            </view>
            <view class="dish_info">
              <view class="dish_name" @click="openDetailHandle(item)">{{
                item.name
              }}</view>
              <view class="dish_label" @click="openDetailHandle(item)">{{
                item.description || item.name
              }}</view>
              <view class="dish_label" @click="openDetailHandle(item)">月销量{{ item.sales }}</view>
              <view class="dish_price">
                <text class="ico">￥</text>
                {{ item.price.toFixed(2) }}
              </view>
              <!-- 菜品列表的增、减 -->
              <view class="dish_active" v-if="!item.flavors || item.flavors.length === 0">
                <!-- 减菜 -->
                <image v-if="item.dishNumber >= 1" src="../../static/btn_red.png" @click="redDishAction(item, '普通')"
                  class="dish_red"></image>
                <text v-if="item.dishNumber > 0" class="dish_number">{{ item.dishNumber }}</text>
                <!-- 加菜 -->
                <image src="../../static/btn_add.png" class="dish_add" @click="addDishAction(item, '普通')"></image>
              </view>
              <view class="dish_active_btn" v-else>
                <view class="check_but" @click="moreNormDataesHandle(item)">选择规格</view>
              </view>
            </view>
          </view>
          <view class="seize_seat"></view>
        </scroll-view>
        <view class="no_dish" v-else>
          <view v-if="typeListData.length > 0">该分类下暂无菜品</view>
        </view>
      </view>
      <view class="restaurant_close">店铺已打烊</view>
      <!-- end -->
      <view class="mask-box"></view>
      <!-- 底部去结算 -->
      <!-- 购物车里没有订单的状态 -->
      <view class="footer_order_buttom" v-if="orderListData.length === 0 || shopStatus !== 1">
        <view class="order_number">
          <image src="../../static/btn_waiter_nor.png" class="order_number_icon" mode=""></image>
        </view>
        <view class="order_price">
          <text class="ico">￥</text>
          0
        </view>
        <view class="order_but">去结算</view>
      </view>
      <!-- end -->
      <!-- 购物车里有订单结算 -->
      <view class="footer_order_buttom order_form" v-else>
        <view class="orderCar" @click="() => (openOrderCartList = !openOrderCartList)">
          <view class="order_number">
            <image src="../../static/btn_waiter_sel.png" class="order_number_icon" mode=""></image>
            <view class="order_dish_num">{{ orderDishNumber }}</view>
          </view>
          <view class="order_price">
            <text class="ico">￥</text>
            {{ orderDishPrice.toFixed(2) }}
          </view>
        </view>
        <view class="order_but" @click="goOrder()">去结算</view>
      </view>
      <!-- end -->
      <!-- 选择多规格弹层 - start -->
      <view class="pop_mask" v-show="openMoreNormPop">
        <popMask :moreNormDishdata="moreNormDishdata" :moreNormdata="moreNormdata" :flavorDataes="flavorDataes"
          @checkMoreNormPop="checkMoreNormPop" @addShop="addShop" @closeMoreNorm="closeMoreNorm"></popMask>
      </view>
      <!-- 选择多规格 - end -->
      <!-- 菜品详情弹层 - start -->
      <!-- openDetailHandle 这个函数触发的菜品详情 -->
      <view class="pop_mask" v-show="openDetailPop" style="z-index: 9999">
        <dishDetail :dishDetailes="dishDetailes" :openDetailPop="openDetailPop" :dishMealData="dishMealData"
          @redDishAction="redDishAction" @addDishAction="addDishAction" @moreNormDataesHandle="moreNormDataesHandle"
          @dishClose="dishClose"></dishDetail>
      </view>
      <!-- 菜品详情 - end -->
      <!-- 购物车弹框 - start -->
      <view class="pop_mask" v-show="openOrderCartList" @click="openOrderCartList = !openOrderCartList">
        <popCart :openOrderCartLis="openOrderCartList" :orderAndUserInfo="orderAndUserInfo"
          @clearCardOrder="clearCardOrder" @addDishAction="addDishAction" @redDishAction="redDishAction"></popCart>
      </view>
      <!-- 购物车弹框 - end -->
      <view class="pop_mask" v-show="loaddingSt">
        <view class="lodding">
          <image class="lodding_ico" src="../../static/lodding.gif" mode=""></image>
        </view>
      </view>
      <!-- 电话弹层 -->
      <phone ref="phone" :phoneData="phoneData" @closePopup="closePopup"></phone>
      <!-- end -->
      <!-- 店面打烊弹层 -->
      <view class="colseShop" v-if="shopStatus === 0">
        <view class="shop">本店已打样</view>
      </view>
      <!-- end -->
      <!-- 微信授权登录弹窗 -->
      <view class="auth_modal" v-if="showAuthModal">
        <view class="auth_modal_mask" @click="showAuthModal = false"></view>
        <view class="auth_modal_content">
          <view class="auth_modal_title">温馨提示</view>
          <view class="auth_modal_desc">亲，授权登录后才能点餐！</view>
          <button open-type="getPhoneNumber" :loading="loginLoading" @getphonenumber="onGetPhoneNumber" class="auth_phone_btn">
            授权登录
          </button>
        </view>
      </view>
      <!-- end -->
    </view>
  </view>
</template>
<script src="./index.js"></script>
<style src="./style.scss" lang="scss" scoped></style>
<style scoped>
/* #ifdef MP-WEIXIN || APP-PLUS */
::v-deep ::-webkit-scrollbar {
  display: none !important;
  width: 0 !important;
  height: 0 !important;
  -webkit-appearance: none;
  background: transparent;
  color: transparent;
}
.recommend-section {
  margin: 16rpx 20rpx;
  padding: 24rpx 0 20rpx 24rpx;
  background: linear-gradient(135deg, #fff9f5 0%, #ffffff 40%);
  border-radius: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(255, 90, 42, 0.08);
}

.recommend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 24rpx;
  margin-bottom: 20rpx;
}

.recommend-title {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.title-icon {
  font-size: 34rpx;
}

.title-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1a1a;
}

.title-sub {
  font-size: 22rpx;
  color: #999999;
  margin-left: 8rpx;
}

.recommend-more {
  font-size: 24rpx;
  color: #ff5a2a;
  font-weight: 500;
}

.recommend-scroll {
  white-space: nowrap;
}

.recommend-list {
  display: flex;
  gap: 20rpx;
  padding-right: 24rpx;
}

.recommend-item {
  display: inline-block;
  width: 240rpx;
  background: #ffffff;
  border-radius: 16rpx;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.recommend-img-wrap {
  width: 240rpx;
  height: 160rpx;
  overflow: hidden;
  background: #f5f5f5;
}

.recommend-img {
  width: 100%;
  height: 100%;
  display: block;
}

.recommend-info {
  padding: 14rpx 14rpx 16rpx;
}

.recommend-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #1a1a1a;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommend-sales-row {
  margin-top: 4rpx;
}

.recommend-sales {
  font-size: 20rpx;
  color: #999999;
}

.recommend-reason {
  display: flex;
  align-items: flex-start;
  gap: 4rpx;
  margin: 8rpx 0;
  padding: 8rpx 10rpx;
  background: linear-gradient(90deg, #fff4ec 0%, #fff8f2 100%);
  border-radius: 8rpx;
  min-height: 56rpx;
}

.reason-icon {
  font-size: 20rpx;
  flex-shrink: 0;
  line-height: 1.6;
}

.reason-text {
  font-size: 20rpx;
  color: #ff5a2a;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-all;
  overflow: hidden;
}

.recommend-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6rpx;
}

.recommend-price-wrap {
  display: flex;
  align-items: baseline;
}

.recommend-price-symbol {
  font-size: 22rpx;
  font-weight: 600;
  color: #ff5a2a;
}

.recommend-price {
  font-size: 30rpx;
  font-weight: 700;
  color: #ff5a2a;
}

.recommend-add {
  width: 44rpx;
  height: 44rpx;
  background: linear-gradient(135deg, #ff7a45 0%, #ff5a2a 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4rpx 10rpx rgba(255, 90, 42, 0.25);
}

.add-icon {
  color: #ffffff;
  font-size: 32rpx;
  font-weight: 600;
  line-height: 1;
  margin-top: -3rpx;
}
/* #endif */
</style>
