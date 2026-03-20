<script setup>
import Aside from './components/aside.vue';
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const showAside = computed(() => route.path !== '/login');
</script>

<template>
    <div class="app-shell" :class="{ 'is-login': !showAside }">
        <Aside v-if="showAside" />
        <main class="app-main">
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

.app-shell.is-login .app-main {
    padding: 0;
}
</style>
