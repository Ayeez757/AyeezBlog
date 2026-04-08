<script setup>
import Header from './components/header.vue';
import Footer from './components/Footer.vue';
import MagneticCursor from './components/MagneticCursor.vue';
</script>

<template>
  <div class="app-root">
    <MagneticCursor />
    <header>
      <Header />
    </header>

    <main class="app-main">
      <router-view />
    </main>

    <Footer />
  </div>
</template>

<style>
.app-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
  /* 给固定头部让出空间，避免内容被遮挡 */
  padding-top: 68px;
}
</style>
