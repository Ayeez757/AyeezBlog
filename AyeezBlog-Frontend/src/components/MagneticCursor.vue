<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';

const elRef = ref(null);
const dotRef = ref(null);

onMounted(() => {
  const el = elRef.value;
  if (!el) return;
  const dotEl = dotRef.value;
  if (!dotEl) return;

  // 仅在“真实鼠标”环境启用（触屏/笔不展示）
  const finePointer = window.matchMedia?.('(pointer:fine)').matches ?? true;
  if (!finePointer) return;

  const IDLE_SIZE = 34;
  const DOT_SIZE = 5;
  const DOT_COLOR = '#17f700';

  let mouseX = 0;
  let mouseY = 0;
  let renderX = 0;
  let renderY = 0;

  /** @type {HTMLElement|null} */
  let currentTarget = null;
  /** @type {{left:number; top:number; width:number; height:number}|null} */
  let targetRect = null;

  let rafId = 0;

  const refreshTargetRect = () => {
    if (!currentTarget) {
      targetRect = null;
      return;
    }
    const r = currentTarget.getBoundingClientRect();
    targetRect = { left: r.left, top: r.top, width: r.width, height: r.height };
    // 轻微外扩，让“框选”更舒服
    const pad = Math.max(10, Math.round(window.innerWidth / 80));
    el.style.setProperty('--mc-width', `${Math.round(r.width + pad)}px`);
    el.style.setProperty('--mc-height', `${Math.round(r.height + pad)}px`);
  };

  const setTarget = (nextTarget) => {
    if (currentTarget === nextTarget) return;
    currentTarget = nextTarget;
    if (currentTarget) {
      refreshTargetRect();
      el.classList.add('is-locked');
    } else {
      el.classList.remove('is-locked');
      el.style.setProperty('--mc-width', `${IDLE_SIZE}px`);
      el.style.setProperty('--mc-height', `${IDLE_SIZE}px`);
      targetRect = null;
    }
  };

  const MAGNETIC_SELECTOR = [
    // 显式标记（优先）
    '[data-magnetic-cursor]',
    '.magnetic-cursor-target',

    // 常见可点击
    'button',
    'a[href]',
    '[role="button"]',
    '[onclick]',

    // 表单交互
    'input:not([type="hidden"])',
    'textarea',
    'select',
    'label',

    // 可聚焦/可操作
    'summary',
    'details',
    '[tabindex]:not([tabindex="-1"])',

    // 常见 UI 库/命名（按需增补）
    '.btn',
    '.button',
    '.el-button',
    '.ant-btn',
  ].join(',');

  const isMagneticTarget = (node) => {
    if (!(node instanceof Element)) return false;
    return Boolean(node.closest?.(MAGNETIC_SELECTOR));
  };

  const pickTarget = (node) => {
    if (!(node instanceof Element)) return null;
    return node.closest?.(MAGNETIC_SELECTOR) ?? null;
  };

  const onPointerMove = (e) => {
    // 只响应鼠标
    if (e.pointerType && e.pointerType !== 'mouse') return;
    mouseX = e.clientX;
    mouseY = e.clientY;

    // 中心点：不走任何缓动，真实跟随鼠标
    dotEl.style.transform = `translate3d(${mouseX}px, ${mouseY}px, 0)`;
  };

  const onPointerOver = (e) => {
    if (e.pointerType && e.pointerType !== 'mouse') return;
    if (!isMagneticTarget(e.target)) return;
    const target = pickTarget(e.target);
    if (target) setTarget(target);
  };

  const onPointerOut = (e) => {
    if (e.pointerType && e.pointerType !== 'mouse') return;
    if (!currentTarget) return;
    const next = e.relatedTarget;
    if (next instanceof Node && currentTarget.contains(next)) return; // 仍在目标内部移动
    setTarget(null);
  };

  const tick = () => {
    rafId = window.requestAnimationFrame(tick);

    let targetX = mouseX;
    let targetY = mouseY;

    if (currentTarget) {
      // 滚动/布局变化时，rect 可能变；低成本刷新一下
      refreshTargetRect();
      if (targetRect) {
        const cx = targetRect.left + targetRect.width / 2;
        const cy = targetRect.top + targetRect.height / 2;
        // 进入目标后：光标锁定在按钮中心（“范围内怎么移动都固定”）
        targetX = cx;
        targetY = cy;
      }
    }

    // 平滑跟随（越大越“磁”）
    const ease = currentTarget ? 0.22 : 0.28;
    renderX += (targetX - renderX) * ease;
    renderY += (targetY - renderY) * ease;
    el.style.transform = `translate3d(${renderX}px, ${renderY}px, 0)`;
  };

  // 初始化：给一个可见位置（避免初始闪到左上角）
  renderX = window.innerWidth / 2;
  renderY = window.innerHeight / 2;
  el.style.setProperty('--mc-width', `${IDLE_SIZE}px`);
  el.style.setProperty('--mc-height', `${IDLE_SIZE}px`);

  // 让点先出现在屏幕中间
  dotEl.style.width = `${DOT_SIZE}px`;
  dotEl.style.height = `${DOT_SIZE}px`;
  dotEl.style.top = `${-DOT_SIZE / 2}px`;
  dotEl.style.left = `${-DOT_SIZE / 2}px`;
  dotEl.style.background = DOT_COLOR;
  dotEl.style.transform = `translate3d(${renderX}px, ${renderY}px, 0)`;

  window.addEventListener('pointermove', onPointerMove, { passive: true });
  window.addEventListener('pointerover', onPointerOver, true);
  window.addEventListener('pointerout', onPointerOut, true);
  window.addEventListener('scroll', refreshTargetRect, { passive: true });
  window.addEventListener('resize', refreshTargetRect, { passive: true });
  rafId = window.requestAnimationFrame(tick);

  onBeforeUnmount(() => {
    window.removeEventListener('pointermove', onPointerMove);
    window.removeEventListener('pointerover', onPointerOver, true);
    window.removeEventListener('pointerout', onPointerOut, true);
    window.removeEventListener('scroll', refreshTargetRect);
    window.removeEventListener('resize', refreshTargetRect);
    if (rafId) window.cancelAnimationFrame(rafId);
  });
});
</script>

<template>
  <div ref="elRef" class="magnetic-cursor" aria-hidden="true">
    <div class="corner tl" />
    <div class="corner tr" />
    <div class="corner bl" />
    <div class="corner br" />
  </div>
  <div ref="dotRef" class="magnetic-cursor-dot" aria-hidden="true" />
</template>

<style scoped>
.magnetic-cursor {
  --mc-width: 34px;
  --mc-height: 34px;
  --mc-corner: 14px;
  --mc-border: 2px;
  --mc-color: #17f700;

  position: fixed;
  top: calc(var(--mc-height) / -2);
  left: calc(var(--mc-width) / -2);
  width: var(--mc-width);
  height: var(--mc-height);
  pointer-events: none;
  z-index: 99999;

  transition:
    width 0.18s ease-out,
    height 0.18s ease-out,
    opacity 0.2s ease-out;
  will-change: transform, width, height;
}

.magnetic-cursor-dot {
  position: fixed;
  top: -2.5px;
  left: -2.5px;
  width: 5px;
  height: 5px;
  border-radius: 999px;
  background: #17f700;
  opacity: 1;
  box-shadow:
    0 0 6px rgba(23, 247, 0, 0.9),
    0 0 14px rgba(23, 247, 0, 0.55);
  pointer-events: none;
  z-index: 100000;
  will-change: transform;
}

.corner {
  position: absolute;
  width: var(--mc-corner);
  height: var(--mc-corner);
  border-color: var(--mc-color);
  border-width: var(--mc-border);
}

.tl {
  top: 0;
  left: 0;
  border-top-style: solid;
  border-left-style: solid;
}
.tr {
  top: 0;
  right: 0;
  border-top-style: solid;
  border-right-style: solid;
}
.bl {
  bottom: 0;
  left: 0;
  border-bottom-style: solid;
  border-left-style: solid;
}
.br {
  bottom: 0;
  right: 0;
  border-bottom-style: solid;
  border-right-style: solid;
}

/* 目标锁定时稍微更“锐利” */
.magnetic-cursor.is-locked {
  opacity: 1;
}

@media (prefers-reduced-motion: reduce) {
  .magnetic-cursor {
    transition: none;
  }
}
</style>
