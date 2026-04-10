-- 将明文密码 admin 升级为 BCrypt（与 AyeezBlog建表.sql 中默认管理员一致）
-- 仅在仍使用明文 admin 时执行；若已改过密码请自行生成哈希后更新。
UPDATE `user`
SET password = '$2a$10$eBH3YL6wptUNHJidoKGlzeVue1QUxgDEpHOfr0zefa6eM1IxuNfga'
WHERE username = 'admin'
  AND password = 'admin';
