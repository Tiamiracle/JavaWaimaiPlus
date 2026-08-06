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
        <scroll-view class="recommend-scroll" scroll-x show-scrollbar="false" :scroll-with-animation="true">
          <view class="recommend-list">
            <view class="recommend-item" v-for="dish in recommendList" :key="dish.id" @tap="openDetailHandle(dish)">
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
                  <!-- 有口味数据时显示"选规格"按钮 -->
                  <view class="recommend-btn-spec" v-if="dish.flavors && dish.flavors.length > 0" @tap.stop="openSpecPop(dish)">
                    <text>选规格</text>
                  </view>
                  <!-- 无口味数据时显示"+"按钮 -->
                  <view class="recommend-add" v-else @tap.stop="handleRecommendAdd(dish)">
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
      <view class="restaurant_close" v-if="shopStatus !== 1">店铺已打烊</view>
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
      <view class="pop_mask" v-show="openMoreNormPop"@click.self="closeMoreNorm()">
        <popMask :moreNormDishdata="moreNormDishdata" :moreNormdata="moreNormdata" :flavorDataes="flavorDataes"
          @checkMoreNormPop="checkMoreNormPop" @addShop="addShop" @closeMoreNorm="closeMoreNorm"></popMask>
      </view>
      <!-- 选择多规格 - end -->
      <!-- 菜品详情弹层 - start -->
      <!-- openDetailHandle 这个函数触发的菜品详情 -->
      <view class="pop_mask" v-show="openDetailPop" style="z-index: 9999"@click.self="dishClose()">
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
    <chat-ai :is-shop-open="shopStatus === 1" @need-login="handleNeedLogin"></chat-ai>
  </view>
</template>
<script src="./index.js"></script>
<style src="./style.scss" lang="scss" scoped></style>
