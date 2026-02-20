<template>
    <header class="blog-header">

        <!-- 这个div是套着竖线和标题的，保障粘一块 -->
        <div style="display: flex;flex-direction: row;gap: 2vw;">
            <!-- 炫光竖线 -->
            <div class="glow-line"></div>

            <!-- 左上角标题 -->
            <div class="header-title">
                <h1 @click="goHome" ref="title">阿叶Ayeez Blog</h1>
            </div>
        </div>

        <!-- 导航栏的导航项 -->
        <nav class="header-nav">
            <ul>
                <li v-for="(item, index) in navItems" :key="index" class="nav-item">
                    <a :href="`#${item.section}`" @click="navigate(item.section)">{{ item.name }}</a>
                    
                    <div class="fluorescent-bar"></div>
                </li>
            </ul>
        </nav>

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
                { name: "首页", section: "home" },
                { name: "关于", section: "about" },
                { name: "归档", section: "archive" },
                { name: "友链", section: "links" },
                { name: "朋友圈", section: "fc" },
                { name: "留言", section: "comments" },
                { name: "日志", section: "logs" },
                // { name: "联系", section: "contact" }
            ]
        };
    },
    mounted() {
        /// 确保 DOM 渲染完成后再执行动画
        this.$nextTick(() => {
            this.animateTitle(); // 执行标题动画
            this.animateNavItems(); // 执行导航项动画
        });
        
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
            this.$router.push(`/#${section}`);
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
    box-shadow: 0 0 10px #6bff7a, 0 0 20px #00ff80, 0 0 30px #6bff7a;
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
    background-color: rgb(0, 0, 0);
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
    background: linear-gradient(90deg, #06cc1ab8, #25fc90a2, #06cc1aa6);
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
  opacity: 0; /* 初始隐藏 */
  transform: translateX(100%); /* 初始向右偏移 */
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
</style>