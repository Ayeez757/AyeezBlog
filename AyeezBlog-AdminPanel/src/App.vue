<script setup>
import Aside from './components/aside.vue';
// import Home from './views/Home.vue';


</script>

<template>
    <div class="container">


        <Aside />

        <router-view />
    </div>


</template>

<style scoped>
.container {
    display: flex;
    flex-direction: row;
}
</style>
