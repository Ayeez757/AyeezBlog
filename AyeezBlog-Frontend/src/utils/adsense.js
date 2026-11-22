const ADSENSE_CLIENT = 'ca-pub-7074159807555116';
const ADSENSE_SCRIPT_ID = 'google-adsense-script';
const ADSENSE_SRC = `https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=${ADSENSE_CLIENT}`;

let adsenseInjected = false;

/**
 * 仅在允许展示广告的内容页（如首页、正式文章详情页）按需注入 adsbygoogle.js。
 * 站点所有权验证由 index.html 中的 <meta name="google-adsense-account"> 全站保障。
 */
export function ensureAdsenseLoaded() {
  if (typeof document === 'undefined') return;
  if (adsenseInjected || document.getElementById(ADSENSE_SCRIPT_ID)) {
    adsenseInjected = true;
    return;
  }

  const existing = document.querySelector(
    'script[src*="pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"]'
  );
  if (existing) {
    adsenseInjected = true;
    return;
  }

  const script = document.createElement('script');
  script.id = ADSENSE_SCRIPT_ID;
  script.async = true;
  script.src = ADSENSE_SRC;
  script.crossOrigin = 'anonymous';
  document.head.appendChild(script);
  adsenseInjected = true;
}
