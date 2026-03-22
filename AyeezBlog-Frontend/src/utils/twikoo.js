/**
 * Twikoo：Vite 动态 import 时 UMD 的 default / 命名空间形态不一致，需多路径解析。
 * 本地 localhost 直连 https://twikoo 会 CORS，开发/预览走 /twikoo-proxy（见 vite.config.js）。
 */

const PROD_TWIKOO_URL = 'https://twikoo.ayeez.cn';

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
 * 浏览器端 Twikoo envId：生产用官方域名；本机 dev/preview 走同源代理避免 CORS。
 * 可通过 .env 设置 VITE_TWIKOO_URL 覆盖。
 */
export function getTwikooEnvId() {
  const fromEnv = import.meta.env.VITE_TWIKOO_URL;
  if (fromEnv) return fromEnv.replace(/\/$/, '');

  if (typeof window === 'undefined') return PROD_TWIKOO_URL;

  const host = window.location.hostname;
  const isLocal = host === 'localhost' || host === '127.0.0.1';
  if (isLocal) {
    return `${window.location.origin}/twikoo-proxy`;
  }
  return PROD_TWIKOO_URL;
}
