<template>
  <div class="login-container">
    <div class="login-box page-card">
      <h2>后台登录</h2>
      <el-form @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="用户名">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-button class="submit-btn" type="primary" :loading="loading" @click="handleLogin">
          {{ loading ? '登录中...' : '登录' }}
        </el-button>
      </el-form>
      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginApi } from '@/api/index'

export default {
  name: 'LoginPage',
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const errorMsg = ref('')
    const loginForm = ref({
      username: '',
      password: ''
    })

    const handleLogin = async () => {
      loading.value = true
      errorMsg.value = ''
      
      try {
        const res = await loginApi(loginForm.value)
        // 登录成功，存储 token（res 已经是 data，不是 response）
        localStorage.setItem('token', res.token)
        // 跳转到首页
        router.push('/')
      } catch (error) {
        errorMsg.value = error.message || '登录失败，请检查用户名和密码'
      } finally {
        loading.value = false
      }
    }

    return {
      loading,
      errorMsg,
      loginForm,
      handleLogin
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #eef4ff 0%, #f7f9fc 100%);
}

.login-box {
  width: 420px;
  padding: 28px;
}

h2 {
  margin: 0 0 18px;
  font-size: 22px;
  color: #111827;
}

.submit-btn {
  width: 100%;
}

.error-msg {
  color: #f5222d;
  margin: 12px 0 0;
  text-align: left;
  font-size: 13px;
}

@media (max-width: 768px) {
  .login-container {
    padding: 14px;
  }

  .login-box {
    width: 100%;
    padding: 18px;
  }
}
</style>