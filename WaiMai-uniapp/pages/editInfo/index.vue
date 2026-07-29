<template>
  <view class="edit-info">
    <uni-nav-bar @clickLeft="goBack" left-icon="back" leftIcon="arrowleft" title="修改信息" statusBar="true" fixed="true"
      color="#ffffff" backgroundColor="#ffc200"></uni-nav-bar>

    <view class="content">
      <!-- 头像上传 -->
      <view class="form-item">
        <text class="label">头像</text>
        <view class="right">
          <view class="avatar-wrapper" @click="uploadAvatar">
            <image class="avatar" :src="avatar" mode="aspectFill"></image>
            <view class="upload-icon">
              <text class="icon-text">+</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 昵称 -->
      <view class="form-item">
        <text class="label">昵称</text>
        <input class="input" v-model="name" placeholder="请输入昵称" />
      </view>

      <!-- 电话（只读） -->
      <view class="form-item">
        <text class="label">电话</text>
        <input style="color:#999;" :maxlength="11" class="input" :value="phone" disabled placeholder="电话号码" />
      </view>

      <!-- 身份证号 -->
      <view class="form-item">
        <text class="label">身份证号</text>
        <input class="input" v-model="idNumber" :maxlength="18" placeholder="请输入身份证号" />
      </view>

      <!-- 性别 -->
      <view class="form-item">
        <text class="label">性别</text>
        <view class="gender-wrapper">
          <view class="gender-option" :class="{ active: sex === '0' }" @click="sex = '0'">
            <text>男</text>
          </view>
          <view class="gender-option" :class="{ active: sex === '1' }" @click="sex = '1'">
            <text>女</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-btn">
      <view class="cancel-btn" @click="goBack">
        <text>取消</text>
      </view>
      <view class="save-btn" @click="save">
        <text>保存</text>
      </view>
    </view>
  </view>
</template>

<script>
import { getUserInfoById, updateUserInfo } from "../api/api.js";
import { baseUrl } from "../../utils/env.js";

export default {
  data() {
    return {
      userId: 0,
      avatar: "",
      name: "",
      phone: "",
      idNumber: "",
      sex: "0",
      user: null
    };
  },
  onLoad() {
    this.userId = this.$store.state.userId;
    this.getUserInfo();
  },
  methods: {
    getUserInfo() {
      if (!this.userId) return;
      getUserInfoById(this.userId).then((res) => {
        if (res.code === 1) {
          this.user = res.data;
          this.avatar = res.data.avatarUrl || res.data.avatar || "";
          this.name = res.data.nickName || res.data.name || "";
          this.phone = res.data.phone || "";
          this.idNumber = res.data.idNumber || res.data.id_number || "";
          this.sex = String(res.data.sex ?? "0");
        }
      });
    },
    uploadAvatar() {
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          const tempFilePaths = res.tempFilePaths;
          const token = this.$store.state.token;
          uni.uploadFile({
            url: baseUrl + '/user/common/upload',
            filePath: tempFilePaths[0],
            name: 'file',
            header: {
              'authentication': token
            },
            success: (uploadRes) => {
              try {
                const data = JSON.parse(uploadRes.data);
                if (data.code === 1) {
                  this.avatar = data.data;
                  uni.showToast({ title: '上传成功', icon: 'success' });
                } else {
                  uni.showToast({ title: '上传失败', icon: 'none' });
                }
              } catch (e) {
                uni.showToast({ title: '上传失败', icon: 'none' });
              }
            },
            fail: () => {
              uni.showToast({ title: '上传失败', icon: 'none' });
            }
          });
        }
      });
    },
    save() {
      // 校验昵称
      if (!this.name) {
        uni.showToast({ title: '请输入昵称', icon: 'none' });
        return;
      }
      // 校验头像
      if (!this.avatar) {
        uni.showToast({ title: '请上传头像', icon: 'none' });
        return;
      }
      if (this.idNumber != null && this.idNumber !== "") {
        const value = this.idNumber;
        const reg = /^\d{17}(\d|X|x)$/
        if (!reg.test(value)) {
          uni.showToast({ title: '请输入正确的身份证号', icon: 'none' });
          return;
        }
        const birth = value.substring(6, 14)
        const year = parseInt(birth.substring(0, 4))
        const month = parseInt(birth.substring(4, 6))
        const day = parseInt(birth.substring(6, 8))

        const date = new Date(year, month - 1, day)
        if (
          date.getFullYear() !== year ||
          date.getMonth() !== month - 1 ||
          date.getDate() !== day ||
          year < 1900 ||
          year > new Date().getFullYear()
        ) {
          uni.showToast({ title: '请输入正确的身份证号', icon: 'none' });
          return;
        }
        const factor = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
        const parity = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
        let sum = 0
        for (let i = 0; i < 17; i++) {
          sum += parseInt(value.charAt(i)) * factor[i]
        }
        const checkCode = parity[sum % 11]
        if (checkCode !== value.charAt(17).toUpperCase()) {
          uni.showToast({ title: '请输入正确的身份证号', icon: 'none' });
          return;
        }
      }
      this.user.name = this.name;
      this.user.avatar = this.avatar;
      this.user.sex = this.sex;
      this.user.idNumber = this.idNumber;
      console.log('修改', this.user);
      updateUserInfo(this.userId, this.user).then((res) => {
        if (res.code === 1) {
          uni.showToast({ title: '修改成功', icon: 'success' });
          setTimeout(() => {
            uni.navigateBack();
          }, 1500);
        } else {
          uni.showToast({ title: res.msg || '修改失败', icon: 'none' });
        }
      });
    },
    goBack() {
      uni.navigateBack();
    }
  }
};
</script>

<style lang="scss" scoped>
.edit-info {
  min-height: 100vh;
  background: #f6f6f6;
  padding-top: calc(env(safe-area-inset-top) + 88rpx);
}

.content {
  background: #ffffff;
  margin: 20rpx;
  border-radius: 12rpx;
  padding: 30rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .label {
    width: 140rpx;
    font-size: 28rpx;
    color: #333333;
    flex-shrink: 0;
  }

  .input {
    flex: 1;
    font-size: 28rpx;
    color: #333333;
    text-align: right;
  }
}

.right {
  flex: 1;
  justify-content: flex-end;
  display: flex;
}

.avatar-wrapper {
  position: relative;
  width: 150rpx;
  height: 150rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 2rpx solid #ddd;

  .avatar {
    width: 100%;
    height: 100%;
  }

  .upload-icon {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    align-items: center;
    justify-content: center;

    .icon-text {
      color: #ffffff;
      font-size: 40rpx;
    }
  }
}

.gender-wrapper {
  flex: 1;
  display: flex;
  justify-content: flex-end;

  .gender-option {
    padding: 10rpx 30rpx;
    border: 2rpx solid #ddd;
    border-radius: 30rpx;
    margin-left: 20rpx;
    font-size: 26rpx;
    color: #666666;

    &.active {
      border-color: #ffc200;
      color: #ffc200;
    }
  }
}

.bottom-btn {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  background: #ffffff;
  padding: 20rpx 30rpx;
  padding-bottom: calc(env(safe-area-inset-bottom) + 20rpx);
  border-top: 1px solid #f0f0f0;

  .cancel-btn,
  .save-btn {
    flex: 1;
    height: 88rpx;
    line-height: 88rpx;
    text-align: center;
    border-radius: 44rpx;
    font-size: 30rpx;
    margin: 0 15rpx;
  }

  .cancel-btn {
    background: #f5f5f5;
    color: #666666;
  }

  .save-btn {
    background: #ffc200;
    color: #ffffff;
  }
}
</style>