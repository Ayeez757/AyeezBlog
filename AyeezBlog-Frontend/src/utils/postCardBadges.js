/**
 * 首页文章卡片封面角标（与后端 blog_post 布尔字段对应）
 * @param {object} post
 * @returns {{ key: string, label: string, className: string }[]}
 */
export function getPostCardBadges(post) {
  if (!post) return [];
  const list = [];
  if (post.pinned) {
    list.push({ key: 'pinned', label: '置顶', className: 'post-badge--pinned' });
  }
  if (post.featured) {
    list.push({ key: 'featured', label: '推荐', className: 'post-badge--featured' });
  }
  if (post.editing) {
    list.push({ key: 'editing', label: '正在编辑', className: 'post-badge--editing' });
  }
  if (post.water) {
    list.push({ key: 'water', label: '水', className: 'post-badge--water' });
  }
  return list;
}
