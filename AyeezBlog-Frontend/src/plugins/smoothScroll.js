import { nextTick } from 'vue';
import Lenis from 'lenis';

let lenisInstance = null;
let rafId = null;

export function getLenis() {
  return lenisInstance;
}

/**
 * Lenis 接管 window 滚动时，Vue Router 的 scrollBehavior 只改原生 scrollTop，
 * 视觉上仍会停在旧位置。在路由切换后把 Lenis 滚到目标位置，并返回 false 避免二次应用。
 */
export function resolveScrollBehaviorWithLenis(savedPosition) {
  if (!lenisInstance) {
    if (savedPosition) return savedPosition;
    return { top: 0, left: 0 };
  }

  return new Promise((resolve) => {
    nextTick(() => {
      requestAnimationFrame(() => {
        const top =
          savedPosition != null && typeof savedPosition.top === 'number'
            ? savedPosition.top
            : 0;
        lenisInstance.scrollTo(top, { immediate: true });
        resolve(false);
      });
    });
  });
}

export function initSmoothScroll() {
  if (lenisInstance) {
    return lenisInstance;
  }

  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (prefersReducedMotion) {
    return null;
  }

  lenisInstance = new Lenis({
    duration: 1.2,
    easing: (t) => 1 - Math.pow(1 - t, 5),
    smoothWheel: true,
    wheelMultiplier: 1.4,
    touchMultiplier: 0.5
  });

  const raf = (time) => {
    lenisInstance.raf(time);
    rafId = requestAnimationFrame(raf);
  };
  rafId = requestAnimationFrame(raf);

  return lenisInstance;
}

export function destroySmoothScroll() {
  if (!lenisInstance) return;
  try {
    if (rafId) cancelAnimationFrame(rafId);
  } finally {
    rafId = null;
  }

  // Lenis 自带 destroy，用于停止内部事件/计算
  if (typeof lenisInstance.destroy === 'function') {
    lenisInstance.destroy();
  }
  lenisInstance = null;
}
