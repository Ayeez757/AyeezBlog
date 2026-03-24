<script setup>
import Aside from './components/aside.vue';
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const showAside = computed(() => route.path !== '/login');
const isMobile = ref(false);
const mobileMenuVisible = ref(false);

const updateViewportState = () => {
    isMobile.value = window.innerWidth <= 768;
    if (!isMobile.value) {
        mobileMenuVisible.value = false;
    }
};

const toggleMobileMenu = () => {
    mobileMenuVisible.value = !mobileMenuVisible.value;
};

const closeMobileMenu = () => {
    mobileMenuVisible.value = false;
};

watch(
    () => route.path,
    () => {
        if (isMobile.value) {
            closeMobileMenu();
        }
    }
);

onMounted(() => {
    updateViewportState();
    window.addEventListener('resize', updateViewportState);
});

onBeforeUnmount(() => {
    window.removeEventListener('resize', updateViewportState);
});
</script>

<template>
    <div class="app-shell" :class="{ 'is-login': !showAside, 'is-mobile': isMobile }">
        <Aside
            v-if="showAside"
            :is-mobile="isMobile"
            :visible="!isMobile || mobileMenuVisible"
            @close="closeMobileMenu"
        />
        <div v-if="showAside && isMobile && mobileMenuVisible" class="app-mask" @click="closeMobileMenu"></div>
        <main class="app-main">
            <div v-if="showAside && isMobile" class="mobile-topbar">
                <button class="menu-btn" type="button" @click="toggleMobileMenu">菜单</button>
            </div>
            <router-view />
        </main>
    </div>
</template>

<style scoped>
.app-shell {
    display: flex;
    min-height: 100vh;
    background: #f5f7fb;
}

.app-main {
    flex: 1;
    min-width: 0;
    padding: 20px;
}

.app-mask {
    position: fixed;
    inset: 0;
    background: rgba(15, 23, 42, 0.45);
    z-index: 999;
}

.mobile-topbar {
    margin-bottom: 12px;
}

.menu-btn {
    border: 1px solid #d1d5db;
    border-radius: 8px;
    background: #fff;
    color: #111827;
    font-size: 14px;
    line-height: 1;
    padding: 9px 14px;
    cursor: pointer;
}

.app-shell.is-login .app-main {
    padding: 0;
}

@media (max-width: 768px) {
    .app-main {
        padding: 12px;
    }
}
</style>
