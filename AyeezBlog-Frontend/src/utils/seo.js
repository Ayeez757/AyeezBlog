const SITE_ORIGIN = 'https://blog.ayeez.cn';
const SITE_NAME = '阿叶Ayeez的博客';
const DEFAULT_TITLE = '阿叶Ayeez的博客~';
const DEFAULT_DESCRIPTION =
  '阿叶 Ayeez 的个人技术博客，记录 Java、Spring Boot、MySQL、Docker、网络原理与项目实战学习笔记。';
const JSON_LD_SCRIPT_ID = 'ayeez-page-jsonld';

function ensureMetaTag(name) {
  let el = document.head.querySelector(`meta[name="${name}"]`);
  if (!el) {
    el = document.createElement('meta');
    el.setAttribute('name', name);
    document.head.appendChild(el);
  }
  return el;
}

function removeMetaTag(name) {
  const el = document.head.querySelector(`meta[name="${name}"]`);
  if (el && el.parentNode) {
    el.parentNode.removeChild(el);
  }
}

function ensureCanonicalLink() {
  let el = document.head.querySelector('link[rel="canonical"]');
  if (!el) {
    el = document.createElement('link');
    el.setAttribute('rel', 'canonical');
    document.head.appendChild(el);
  }
  return el;
}

export function buildCanonicalUrl(path = '/') {
  const normalized = path.startsWith('/') ? path : `/${path}`;
  return `${SITE_ORIGIN}${normalized}`;
}

export function clearJsonLd() {
  const existing = document.getElementById(JSON_LD_SCRIPT_ID);
  if (existing && existing.parentNode) {
    existing.parentNode.removeChild(existing);
  }
}

export function setJsonLd(data) {
  clearJsonLd();
  if (!data || typeof data !== 'object') return;
  const script = document.createElement('script');
  script.id = JSON_LD_SCRIPT_ID;
  script.type = 'application/ld+json';
  script.textContent = JSON.stringify(data);
  document.head.appendChild(script);
}

/**
 * 统一更新当前路由的 SEO Meta、Canonical 与结构化数据（无任何页面视觉副作用）
 */
export function updatePageSeo({
  title,
  description,
  canonicalPath,
  robots,
  jsonLd
} = {}) {
  if (typeof document === 'undefined') return;

  const trimmedTitle = title ? String(title).trim() : '';
  document.title = trimmedTitle
    ? (trimmedTitle.includes(SITE_NAME) ? trimmedTitle : `${trimmedTitle} - ${SITE_NAME}`)
    : DEFAULT_TITLE;

  const desc = description && String(description).trim()
    ? String(description).trim().slice(0, 180)
    : DEFAULT_DESCRIPTION;
  ensureMetaTag('description').setAttribute('content', desc);

  const path = canonicalPath || (typeof window !== 'undefined' ? window.location.pathname : '/');
  ensureCanonicalLink().setAttribute('href', buildCanonicalUrl(path));

  if (robots) {
    ensureMetaTag('robots').setAttribute('content', String(robots));
  } else {
    removeMetaTag('robots');
  }

  if (jsonLd !== undefined) {
    setJsonLd(jsonLd);
  } else {
    clearJsonLd();
  }
}

export function extractPlainTextExcerpt(markdownOrHtml, maxLength = 160) {
  if (!markdownOrHtml) return '';
  const text = String(markdownOrHtml)
    .replace(/^---[\s\S]*?---\s*/, '')
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/`[^`]*`/g, ' ')
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/\[([^\]]+)\]\([^)]*\)/g, '$1')
    .replace(/<[^>]+>/g, ' ')
    .replace(/[#>*_~\-]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
  if (!text) return '';
  return text.length > maxLength ? `${text.slice(0, maxLength)}...` : text;
}
