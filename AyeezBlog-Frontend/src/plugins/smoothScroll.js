import Lenis from 'lenis';

let lenisInstance = null;

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
    requestAnimationFrame(raf);
  };
  requestAnimationFrame(raf);

  return lenisInstance;
}
