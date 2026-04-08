import Lenis from 'lenis';

let lenisInstance = null;
let rafId = null;

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
