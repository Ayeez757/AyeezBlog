<template>
    <header class="blog-header">

        <!-- 左侧：炫光竖线 + 标题 -->
        <div class="header-left">
            <!-- 炫光竖线 -->
            <div class="glow-line"></div>

            <!-- 左上角标题 -->
            <div class="header-title">
                <h1 @click="goHome" ref="title">阿叶Ayeez Blog</h1>
            </div>
        </div>

        <!-- 右侧：PC 导航 + 汉堡按钮 -->
        <div class="header-right">
            <!-- 导航栏的导航项（大屏显示） -->
            <nav class="header-nav">
                <ul>
                    <li
                        v-for="(item, index) in navItems"
                        :key="index"
                        class="nav-item"
                    >
                        <a @click.prevent="navigate(item.section)" href="#">{{ item.name }}</a>
                        <div class="fluorescent-bar"></div>
                    </li>
                </ul>
            </nav>

            <!-- 汉堡按钮（小屏显示） -->
            <button
                class="hamburger-btn"
                :class="{ 'is-open': isMenuOpen }"
                @click="toggleMenu"
                aria-label="切换导航菜单"
            >
                <span></span>
                <span></span>
                <span></span>
            </button>
        </div>

        <!-- 侧边抽屉导航 -->
        <transition name="side-drawer">
            <aside
                v-if="isMenuOpen"
                class="side-drawer"
                @click.self="closeMenu"
            >
                <div class="side-drawer-panel">
                    <div class="side-drawer-header">
                        <div class="glow-line small"></div>
                        <span class="side-drawer-title">导航菜单</span>
                    </div>
                    <ul class="side-drawer-list">
                        <li
                            v-for="(item, index) in navItems"
                            :key="'side-' + index"
                            class="side-drawer-item"
                            @click="handleMenuClick(item.section)"
                        >
                            <span>{{ item.name }}</span>
                            <div class="side-drawer-bar"></div>
                        </li>
                    </ul>
                </div>
            </aside>
        </transition>

        <!-- 页头下面的分割细绿线 -->
        <div class="header-divider"></div>

    </header>
</template>

<script>
export default {
    data() {
        return {
            // 把导航项提出来
            navItems: [
                { name: "首页", section: "" },
                { name: "关于", section: "about" },
                { name: "归档", section: "archive" },
                { name: "友链", section: "links" },
                { name: "朋友圈", section: "fc" },
                { name: "留言", section: "comments" },
                { name: "日志", section: "logs" },
                // { name: "联系", section: "contact" }
            ],
            isMenuOpen: false
        };
    },
    mounted() {
        /// 确保 DOM 渲染完成后再执行动画
        this.$nextTick(() => {
            this.animateTitle(); // 执行标题动画
            this.animateNavItems(); // 执行导航项动画
        });

        window.addEventListener("resize", this.handleResize);
    },
    beforeDestroy() {
        window.removeEventListener("resize", this.handleResize);
    },
    methods: {
        animateTitle() {
            const titleElement = this.$refs.title;
            if (!titleElement) return;

            const text = titleElement.textContent;
            titleElement.innerHTML = ""; // 清空原始内容

            text.split("").forEach((char, index) => {
                const span = document.createElement("span");
                span.textContent = char === " " ? "\u00A0" : char;

                // 初始状态：透明 + 下方偏移 + 缩小
                span.style.opacity = "0";
                span.style.transform = "translateY(20px) scale(0.5)";
                span.style.display = "inline-block";
                span.style.transition = `all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) ${index * 0.1}s`;

                titleElement.appendChild(span);

                // 触发动画：恢复位置 + 显示 + 放大
                setTimeout(() => {
                    span.style.opacity = "1";
                    span.style.transform = "translateY(0) scale(1)";
                }, index * 100);
            });
        },
        animateNavItems() {
            this.$nextTick(() => {
                const navItems = document.querySelectorAll(".nav-item");

                // 使用保守的延迟时间确保所有项都能显示
                navItems.forEach((item, index) => {
                    // 先重置样式确保正确初始状态
                    item.style.opacity = "0";
                    item.style.transform = "translateX(100%)";
                    item.style.position = "relative";

                    const delay = index * 200 + 400; // 每200ms一个间隔

                    setTimeout(() => {
                        item.style.transition = "all 0.6s ease-out";
                        item.style.transform = "translateX(0)";
                        item.style.opacity = "1";
                    }, delay);
                });
            });
        },
        goHome() {
            this.$router.push("/");
        },
        navigate(section) {
            this.$router.push(`/${section}`);
        },
        toggleMenu() {
            this.isMenuOpen = !this.isMenuOpen;
        },
        closeMenu() {
            this.isMenuOpen = false;
        },
        handleMenuClick(section) {
            this.navigate(section);
            this.closeMenu();
        },
        handleResize() {
            // 大屏时强制关闭抽屉，防止布局错乱
            if (window.innerWidth > 768 && this.isMenuOpen) {
                this.isMenuOpen = false;
            }
        }
    }
};
</script>

<style>
/* 炫光竖线样式 */
.glow-line {
    width: 4px;
    /* 竖线宽度 */
    height: 40px;
    /* 竖线高度 */
    background: linear-gradient(180deg, #06cc1a, #25fc91, rgb(23, 219, 42));
    /* 渐变颜色 */
    border-radius: 2px;
    /* 圆角效果 */
    box-shadow: 5px 0px 10px #6bff7a, 15px 0px 20px #00ff80, 25px 0px 30px #6bff7a;
    /* box-shadow: 5px 0px 10px #6bff7a, 15px 0px 20px #00ff80, 25px 0px 30px #6bff7a; */
    /* 发光效果 */
    animation: glow-pulse 2s infinite alternate;
    /* 动画效果 */
}

/* 炫光脉冲动画 */
@keyframes glow-pulse {
    0% {
        opacity: 0.6;
        transform: scaleY(1);
    }

    100% {
        opacity: 1;
        transform: scaleY(1.2);
    }
}

/* 页头整体样式 */
.blog-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 5px 40px;
    background-color: rgba(0, 0, 0, 0.788);
    /* 半透明黑色背景 */
    color: rgba(235, 235, 235, 0.866);
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    z-index: 1000;
    box-sizing: border-box;
    /* 确保页头在最上层 */
}

.header-left {
    display: flex;
    flex-direction: row;
    gap: 2vw;
    align-items: center;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 16px;
}

/* 标题样式 */
.header-title h1 {
    margin: 0;
    font-size: 24px;
    cursor: pointer;
    transition: color 0.3s ease;
}

.header-title h1:hover {
    color: #6bff7a;
    /* 悬停时变色 */
}

/* 导航栏样式 */
.header-nav ul {
    list-style: none;
    display: flex;
    gap: 30px;

    width: 100%;
    min-width: max-content;

    /* 确保导航栏有足够宽度 */
    /* width: fit-content;
    min-width: 300px; */
}

.header-nav {
    /* 确保导航栏不会被截断 */
    overflow: visible;
    white-space: nowrap;
}

.header-nav a {
    text-decoration: none;
    color: rgb(218, 218, 218);
    font-size: 18px;
    transition: color 0.3s ease;
}

.header-nav a:hover {
    color: #6bff7a;
    /* 悬停时变色 */
}

/* 鼠标悬停时标题字符的特效 */
/* 标题字符默认样式 */
.header-title h1 span {
    display: inline-block;
    font-size: 24px;
    font-weight: bold;
    color: #ffffff;
    /* 默认白色 */
    transition: all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

/* 鼠标悬停时的交互效果 */
.header-title h1:hover span {
    color: #0fd223;
    /* 悬停时变为绿色 */
    text-shadow: 0 0 5px #099f18;
    /* 添加发光效果 */
}

/* 页头下侧横线样式 */
.header-divider {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 0.5px;
    /* 横线高度 */
    background: linear-gradient(90deg, #04650db8, #25fc90a2, #024509a6);
    /* 渐变绿色 */
    /* box-shadow: 0 0 5px #06cc1a, 0 0 10px #25fc91; 发光效果 */
    z-index: 1001;
    /* 确保在页头上方 */
}

/* 导航项低下荧光条探照灯 */
/* 导航项容器样式 */
.nav-item {
    position: relative;
    display: flex;
    flex-direction: column;
    align-items: center;
    opacity: 0;
    /* 初始隐藏 */
    transform: translateX(100%);
    /* 初始向右偏移 */
    transition: all 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    /* 确保有足够的空间显示 */
    /* min-width: 60px; */
    overflow: visible;
}

/* 荧光条样式 */
.fluorescent-bar {
    position: absolute;
    bottom: -19px;
    /* left: 0; */
    transform: translateY(100%);
    /* 默认隐藏 */

    width: 130%;
    /* 荧光条宽度 */
    height: 1px;
    /* 荧光条高度 */
    background: linear-gradient(90deg, #06cc1a, #25fc91, #06cc1a);
    /* 绿色渐变 */
    box-shadow: 0 -15px 30px rgba(6, 204, 26, 1), 0 -20px 40px rgba(37, 252, 145, 1);
    /* 向上发散光效 */
    opacity: 1;
    /* 初始隐藏 */
    /* transition: opacity 0.3s ease; */
    z-index: 1002;
}

/* 鼠标悬停时显示荧光条 */
/* .nav-item:hover .fluorescent-bar {
    opacity: 1 !important;
} */

/* 鼠标悬停时链接样式 */
.header-nav a:hover {
    color: #6bff7a;
    /* 悬停时文字变色 */
}

/* 汉堡按钮 */
.hamburger-btn {
    position: relative;
    width: 32px;
    height: 24px;
    border: none;
    background: transparent;
    cursor: pointer;
    padding: 0;
    display: none; /* 默认在大屏隐藏，媒体查询中显示 */
}

.hamburger-btn span {
    position: absolute;
    left: 0;
    width: 100%;
    height: 3px;
    background: linear-gradient(90deg, #06cc1a, #25fc91, #06cc1a);
    border-radius: 999px;
    box-shadow: 0 0 8px rgba(6, 204, 26, 0.8);
    transition: transform 0.25s ease, opacity 0.2s ease, top 0.25s ease, background 0.25s ease;
}

.hamburger-btn span:nth-child(1) {
    top: 0;
}

.hamburger-btn span:nth-child(2) {
    top: 10px;
}

.hamburger-btn span:nth-child(3) {
    top: 20px;
}

.hamburger-btn.is-open span:nth-child(1) {
    top: 10px;
    transform: rotate(45deg);
}

.hamburger-btn.is-open span:nth-child(2) {
    opacity: 0;
}

.hamburger-btn.is-open span:nth-child(3) {
    top: 10px;
    transform: rotate(-45deg);
}

/* 侧边抽屉基础样式 */
.side-drawer {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(4px);
    z-index: 1200;
    display: flex;
    justify-content: flex-end;
}

.side-drawer-panel {
    width: 70%;
    max-width: 320px;
    height: 100%;
    background: radial-gradient(circle at top left, rgba(11, 184, 47, 0.25), rgba(0, 0, 0, 0.92));
    box-shadow: -4px 0 20px rgba(0, 0, 0, 0.8);
    border-left: 1px solid rgba(37, 252, 145, 0.3);
    display: flex;
    flex-direction: column;
    padding: 18px 18px 28px;
}

.side-drawer-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 18px;
}

.glow-line.small {
    height: 26px;
    width: 3px;
    box-shadow: 3px 0px 8px #6bff7a, 10px 0px 16px #00ff80;
}

.side-drawer-title {
    font-size: 18px;
    color: #e9ffe9;
    letter-spacing: 0.08em;
}

.side-drawer-list {
    list-style: none;
    margin: 0;
    padding: 8px 0 0;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.side-drawer-item {
    position: relative;
    padding: 10px 4px;
    color: #f1fff1;
    font-size: 16px;
    display: flex;
    flex-direction: column;
    cursor: pointer;
    transition: color 0.2s ease;
}

.side-drawer-item span {
    z-index: 1;
}

.side-drawer-bar {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 0;
    height: 1px;
    background: linear-gradient(90deg, #06cc1a, #25fc91, #06cc1a);
    box-shadow: 0 -10px 24px rgba(6, 204, 26, 0.9);
    transition: width 0.3s ease;
}

.side-drawer-item:hover {
    color: #6bff7a;
}

.side-drawer-item:hover .side-drawer-bar {
    width: 100%;
}

/* 侧边抽屉过渡动画 */
.side-drawer-enter-active,
.side-drawer-leave-active {
    transition: opacity 0.25s ease;
}

.side-drawer-enter,
.side-drawer-leave-to {
    opacity: 0;
}

.side-drawer-panel {
    transform: translateX(0);
    transition: transform 0.25s ease;
}

.side-drawer-enter .side-drawer-panel,
.side-drawer-leave-to .side-drawer-panel {
    transform: translateX(100%);
}

/* 响应式：小屏使用汉堡菜单，大屏保持原布局 */
@media (max-width: 768px) {
    .blog-header {
        padding: 5px 18px;
        /* 减小左右内边距，让左侧炫光灯和标题更贴近屏幕左边 */
    }

    .header-left {
        gap: 10px;
        /* 收紧炫光灯与标题之间的间距 */
    }

    .header-nav {
        display: none;
    }

    .hamburger-btn {
        display: block;
    }
}
</style>