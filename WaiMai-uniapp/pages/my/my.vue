<template>
  <view>
    <uni-nav-bar @clickLeft="goBack" left-icon="back" leftIcon="arrowleft" title="个人管理" statusBar="true" fixed="true"
      color="#ffffff" backgroundColor="#ffc200"></uni-nav-bar>

    <view class="my-center">
      <!-- 头像展示部分 -->

      <HeadInfo :psersonUrl="psersonUrl" :nickName="nickName" :gender="gender" :phoneNumber="phoneNumber"></HeadInfo>

      <view class="container">
        <!-- 地址和历史订单 -->
        <OrderInfo @goAddress="goAddress" @goOrder="goOrder" @goEdit="goEdit" @logout="logout"></OrderInfo>
        <!-- 最近订单title -->
        <view class="recent" v-if="recentOrdersList && recentOrdersList.length > 0">
          <text class="order_line">最近订单</text>
        </view>
        <OrderList v-if="userId" :scrollH="scrollH" @lower="lower" @goDetail="goDetail" @oneOrderFun="oneOrderFun"
          @getOvertime="getOvertime" @statusWord="statusWord" @numes="numes" :loading="loading"
          :loadingText="loadingText" :recentOrdersList="recentOrdersList"></OrderList>
      </view>
    </view>
    <chat-ai :is-shop-open="true"></chat-ai>
  </view>
</template>

<script>
import { getOrderPage, repetitionOrder, delShoppingCart, getUserInfoById } from "../api/api.js";
import { mapMutations, mapState } from "vuex";
import { statusWord, getOvertime } from "@/utils/index.js";

import HeadInfo from "./components/headInfo.vue"; //头部
import OrderInfo from "./components/orderInfo.vue"; //地址
import OrderList from "./components/orderList.vue"; //最近订单
export default {
  data() {
    return {
      id:0,
      gender: "0",
      psersonUrl: "",
      nickName: "",
      phoneNumber: "",
      recentOrdersList: [],
      sumOrder: {
        amount: 0,
        number: 0,
      },
      status: "",
      scrollH: 0,
      pageInfo: {
        page: 1,
        pageSize: 10,
        total: 0,
      },
      loadingText: "",
      loading: false,
    };
  },
  computed: {
    ...mapState(["userId"]),
  },
  components: {
    HeadInfo,
    OrderInfo,
    OrderList,
  },
  filters: {
    getPhoneNum(str) {
      return str.replace(/\-/g, "");
    },
  },
  onLoad() {
    this.userId = this.$store.state.userId;
    this.getUserInfo();
    this.getList();
  },
  onShow() {
    this.getUserInfo();
  },
  created() { },
  onReady() {
    uni.getSystemInfo({
      success: (res) => {
        this.scrollH = res.windowHeight - uni.upx2px(100);
      },
    });
  },
  methods: {
    ...mapMutations(["setAddressBackUrl"]),
    statusWord(obj) {
      return statusWord(obj.status, obj.time);
    },
    getOvertime(time) {
      return getOvertime(time);
    },
    // 获取用户信息
    getUserInfo() {
      if (!this.userId) return;
      getUserInfoById(this.userId).then((res) => {
        console.log(res)
        if (res.code === 1) {
          this.psersonUrl = res.data.avatar || "";
          this.nickName = res.data.name || "";
          this.phoneNumber = res.data.phone || "";
          this.gender = res.data.sex || "0";
        }
      });
    },
    // 获取列表数据
    getList() {
      const params = {
        pageSize: 10,
        page: this.pageInfo.page,
      };
      getOrderPage(params).then((res) => {
        if (res.code === 1) {
          this.recentOrdersList = this.recentOrdersList.concat(
            res.data.records
          );
          this.pageInfo.total = res.data.total;
          this.loadingText = "";
          this.loading = false;
        }
      });
    },
    // 去地址页面
    goAddress() {
      this.setAddressBackUrl("/pages/my/my");
      // TODO
      uni.redirectTo({
        url: "/pages/address/address?form=" + "my",
      });
    },
    // 去历史订单页面
    goOrder() {
      // TODO
      uni.navigateTo({
        url: "/pages/historyOrder/historyOrder",
      });
    },
    async oneOrderFun(id) {
      uni.showLoading({ title: '加载中...' });
      repetitionOrder(id).then((res) => {
        uni.hideLoading();
        if (res.code === 1) {
          const orderDetailList = res.data.orderDetailList || [];
          const cartData = orderDetailList.map(item => ({
            id: item.id,
            dishId: item.dishId,
            setmealId: item.setmealId,
            name: item.name,
            image: item.image,
            number: item.number,
            amount: item.amount,
            dishFlavor: item.dishFlavor || ''
          }));
          this.$store.commit('initdishListMut', cartData);

          const addressData = {
            id: res.data.addressBookId,
            provinceName: '',
            cityName: '',
            districtName: '',
            detail: res.data.address || '',
            phone: res.data.phone || '',
            consignee: res.data.consignee || '',
            sex: res.data.gender || 0,
            label: ''
          };
          this.$store.commit('setAddress', addressData);

          uni.redirectTo({
            url: '/pages/order/index'
          });
        }
      });
    },
    quitClick() { },
    // 退出登录
    logout() {
      uni.showModal({
        title: "提示",
        content: "确定要退出登录吗？",
        success: (res) => {
          if (res.confirm) {
            // 清空token、userId、以及基础用户信息
            this.$store.commit("setToken", "");
            this.$store.commit("setUserId", 0);
            this.$store.commit("setBaseUserInfo", "");
            uni.redirectTo({
              url: "/pages/index/index",
            });
          }
        },
      });
    },
    // 去修改信息页面
    goEdit() {
      uni.navigateTo({
        url: "/pages/editInfo/index",
      });
    },
    // 去详情页面
    goDetail(id) {
      this.setAddressBackUrl("/pages/my/my");
      uni.redirectTo({
        url: "/pages/details/index?orderId=" + id,
      });
    },
    dataAdd() {
      const pages = Math.ceil(this.pageInfo.total / 10); //计算总页数
      if (this.pageInfo.page === pages) {
        this.loadingText = "没有更多了";
        this.loading = true;
      } else {
        this.pageInfo.page++;
        this.getList();
      }
    },

    lower() {
      this.loadingText = "数据加载中...";
      this.loading = true;
      this.dataAdd();
    },
    goBack() {
      uni.redirectTo({
        url: "/pages/index/index",
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.my-center {
  background: #f6f6f6;
  height: 100%;

  .container {
    margin-top: 20rpx;
    height: calc(100% - 194rpx);
  }
}

::v-deep .uni-navbar--border {
  border-width: 0 !important;
}

.recent {
  margin-left: 40rpx;
}
</style>
