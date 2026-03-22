/**
 * Twikoo：Vite 动态 import 时 UMD 的 default / 命名空间形态不一致，需多路径解析。
 * 本地 localhost 直连 https://twikoo 会 CORS，开发/预览走 /twikoo-proxy（见 vite.config.js）。
 */

const PROD_TWIKOO_URL = 'https://twikoo.ayeez.cn';
/** 与博客不同源时，浏览器直连云函数会触发 CORS（预检失败）；需走同源 /twikoo-proxy 由 Nginx 反代 */
const TWIKOO_HOSTNAME = (() => {
  try {
    return new URL(PROD_TWIKOO_URL).hostname;
  } catch {
    return 'twikoo.ayeez.cn';
  }
})();

let _twikooApiCache = null;

function pickTwikooApi(mod) {
  if (!mod || typeof mod !== 'object') return null;
  const candidates = [mod, mod.default, mod.twikoo, mod.default?.default];
  for (const c of candidates) {
    if (c && typeof c.init === 'function') return c;
  }
  return null;
}

/**
 * @returns {Promise<{ init: Function }>}
 */
export async function loadTwikoo() {
  if (_twikooApiCache) return _twikooApiCache;

  const tryImports = [
    () => import('twikoo'),
    () => import('twikoo/dist/twikoo.all.min.js')
  ];

  let lastErr = null;
  for (const load of tryImports) {
    try {
      const mod = await load();
      const api = pickTwikooApi(mod);
      if (api) {
        _twikooApiCache = api;
        return api;
      }
    } catch (e) {
      lastErr = e;
    }
  }
  throw new Error(
    `Twikoo 模块无效：缺少 init${lastErr ? `（${lastErr.message || lastErr}）` : ''}`
  );
}

/**
 * 浏览器端 Twikoo envId：
 * - 页面与云函数不同源（如 blog.ayeez.cn → twikoo.ayeez.cn）会 CORS，应用 `/twikoo-proxy` + Nginx 反代（见 nginx 示例）。
 * - 仅在当前页面就部署在 Twikoo 域名上时，才直连 PROD_TWIKOO_URL。
 * - 可通过 VITE_TWIKOO_URL 覆盖。
 */
export function getTwikooEnvId() {
  const fromEnv = import.meta.env.VITE_TWIKOO_URL;
  if (fromEnv) return fromEnv.replace(/\/$/, '');

  if (typeof window === 'undefined') return PROD_TWIKOO_URL;

  const host = window.location.hostname;
  if (host === TWIKOO_HOSTNAME) {
    return PROD_TWIKOO_URL;
  }
  // 带尾部斜杠，与 Nginx `location /twikoo-proxy/` 一致，避免 301 把部分客户端 POST 弄丢
  return `${window.location.origin}/twikoo-proxy/`;
}
