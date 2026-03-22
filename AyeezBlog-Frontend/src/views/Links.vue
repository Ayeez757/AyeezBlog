<template>
  <div class="links">
    <div class="links-inner">
      <div class="links-header">
        <div class="links-tagline">WELCOME&nbsp;TO</div>
        <div class="links-title-gradient">AYEEZ&nbsp;LINKS</div>
      </div>
      <p class="links-subtitle">这里是阿叶 Ayeez 的友链小角落，欢迎互相串门。</p>

      <!-- 友链列表（按分组展示） -->
      <div v-for="(group, gIndex) in friendGroups" :key="gIndex" class="links-group">
        <h2 class="group-title">{{ group.class_name }}</h2>
        <p class="group-desc" v-if="group.class_desc">{{ group.class_desc }}</p>

        <div class="links-grid">
          <a v-for="(site, index) in group.link_list" :key="site.link || index" class="link-card" :href="site.link"
            target="_blank" rel="noopener">
            <div class="link-card-header">
              <img class="link-avatar" :src="site.avatar" :alt="site.name" />
              <div class="link-meta">
                <h2 class="link-name">{{ site.name }}</h2>
                <p class="link-descr">
                  {{ site.descr || '这个站长有点酷，还没写简介~' }}
                </p>
              </div>
            </div>
          </a>
        </div>
      </div>

      <!-- 友链说明 & 格式 -->
      <div class="links-rules">
        <h2 class="rules-title">想交换友链吗？</h2>
        <p>
          想交换友链的可以在下方
          <span class="highlight">友链评论区</span>
          底下留言哦！
        </p>
        <p class="rules-subtitle">请先确保以下几点：</p>
        <ul>
          <li>
            你已经将
            <span class="highlight">阿叶Ayeez 的博客</span>
            添加至贵站友链
          </li>
          <li>
            你的网站内容
            <span class="highlight">符合中国大陆法律法规</span>
          </li>
          <li>
            你的网站可以在
            <span class="highlight">1 分钟内加载完成首屏</span>
          </li>
          <li>
            在你的网站里面
            <span class="highlight">加上我的友链</span>
          </li>
        </ul>

        <h3 class="rules-subtitle-strong">我的友链信息：</h3>
        <pre class="links-code">
- name: 阿叶Ayeez的小站
  link: https://blog.Ayeez.cn
  avatar: https://qiniu.ayeez.cn/avatar.jpg
  descr: 记录学习历程，记录美好生活
  siteshot:
        </pre>

        <h3 class="rules-subtitle-strong">你可以按这个格式留言：</h3>
        <pre class="links-code">
- name: //[网站标题]
  link: //[网站网址]
  avatar: //[头像链接]
  descr: //[一句话简介]
  siteshot:
        </pre>

        <p class="links-tip">
          你的友链会在我看到信息的下次更新加上哦~ 如果发现我遗漏了可以戳戳我~
        </p>
      </div>

      <!-- 友链评论区 -->
      <div class="comment-card">
        <h2 class="comment-title">友链评论区</h2>
        <div id="tlinkcomment"></div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Links',
  data() {
    return {
      // 友链分组数据，来自原先的 yml 配置
      friendGroups: [
        {
          class_name: '1.特别关心',
          class_desc: '常联系的朋友~~',
          link_list: [
            {
              name: '阿叶Ayeez的旧站',
              link: 'https://blog.ayeez.cn/',
              avatar: 'https://qiniu.ayeez.cn/avatar.jpg',
              descr: '记录学习历程，记录美好生活'
            },
            {
              name: 'mccsjs',
              link: 'https://seln.cn/',
              avatar: 'https://seln.cn/img/head.jpg',
              descr: '点一盏灯，等一个迷路的夜🍁🍁🍁'
            },
            {
              name: 'ZY知识库',
              link: 'https://blog.pljzy.top/',
              avatar: 'https://blog.pljzy.top/_astro/logo.BxIxyJV1_Z19cEQW.webp',
              descr: '一个技术探索与分享的平台'
            },
            {
              name: '雪萌天文台',
              link: 'https://blog.snowy.moe',
              avatar: 'https://img.snowy.moe/head.png',
              descr: '发现巷子里的那颗星星'
            },
            {
              name: 'ATao-Blog',
              link: 'https://blog.atao.cyou',
              avatar: 'https://cdn.atao.cyou/Web/Avatar.png',
              descr: '做自己喜欢的事'
            },
            {
              name: 'Oxygen‘s Blog',
              link: 'http://beisent.com',
              avatar: 'https://blog.ayeez.cn/imgs/icon/oxygen.png',
              descr: '无'
            },
            {
              name: 'TangShiMei的小空间',
              link: 'https://tang-blog.leleosd.top/',
              avatar: 'https://tang-blog.leleosd.top/img/favicon.ico',
              descr: '想和你重新认识一次，重你叫什么开始'
            },
            {
              name: '苏阳的Blog',
              link: 'https://blog.twis.uk',
              avatar: 'https://blog.ayeez.cn/imgs/icon/twisuki.png',
              descr: '大家好这里是Twisuki'
            },
            {
              name: '裕裕裕的小破宅',
              link: 'https://yu-blog.top/',
              avatar: 'https://yu-blog.top/img/avatar.jpg',
              descr: '一个充满青春活力的技术博客，为初学者提供了优质的博客搭建指南和技术分享。'
            },
            {
              name: '女巫之舞',
              link: 'https://hexentanz.cn/',
              avatar: 'https://hexentanz.cn/images/shuangquanmiao.jpg',
              descr: '喵喵外婆的花田'
            },
            {
              name: '小龙的分享站',
              link: 'https://xiaolongya.cn/',
              avatar: 'https://xiaolongya.cn/uploads/avatar.jpg',
              descr: '来龙岛看看吧，在这里你可以看到龙的开源项目，实用工具，成长感悟与其他分享！'
            }
          ]
        },
        {
          class_name: '2.友情链接',
          class_desc: '一些好朋友~~',
          link_list: [
            {
              name: 'XgrBlog',
              link: 'https://xgrblog.cn',
              avatar: 'https://xgrblog.cn/avatar.jpg',
              descr: '学无止境'
            },
            {
              name: 'YouYou',
              link: 'https://vpnnew.net/blog/',
              avatar: 'https://vpnnew.net/youyou.png',
              descr: ''
            },
            {
              name: 'Revincx',
              link: 'https://blog.revincx.icu/',
              avatar: 'https://cdn.jsdelivr.net/gh/Revincx/blog-assets@master/images/avatar.jpg',
              descr: '可爱就是正义~'
            },
            {
              name: 'Imz',
              link: 'https://blog.imz.me/',
              avatar: 'https://blog.imz.me/img/avatar.webp',
              descr: '仰望星空，脚踏实地。'
            },
            {
              name: '辰渊尘の个人博客',
              link: 'https://blog.mcxiaochen.top/',
              avatar: 'https://blog.mcxiaochen.top/favicon.ico',
              descr: '05后，高中生，内容偏技术向，希望能对你有用QwQ'
            },
            {
              name: '安知鱼',
              link: 'https://blog.anheyu.com/',
              avatar: 'https://npm.elemecdn.com/anzhiyu-blog-static@1.0.4/img/avatar.jpg',
              descr: '生活明朗，万物可爱'
            },
            {
              name: '绘星里',
              link: 'https://blog.storia.ren/',
              avatar: 'https://blog.storia.ren/images/icon.png',
              descr: '一起来绘制属于自己的星星！'
            },
            {
              name: '你好可爱',
              link: 'https://wjldarling.top/',
              avatar: 'https://wangjinglun.oss-cn-beijing.aliyuncs.com/images/1.jpg',
              descr: '山水一程，三生有幸✨'
            },
            {
              name: '善小逸🍊',
              link: 'https://www.donghao.ltd/',
              avatar: 'https://img.donghao.ltd/img/touxiang.jpg',
              descr: '所有打不倒你的难，最终都会变成你身上的光'
            },
            {
              name: '𝟞𝟙𝟡\'𝕤 𝔹𝕃𝕆𝔾',
              link: 'https://66619.eu.org',
              avatar: 'https://blog.ayeez.cn/imgs/icon/619.png',
              descr: '𝓙𝓾𝓼𝓽 𝓪 𝓬𝓵𝓸𝓾𝓭'
            },
            {
              name: '写bug的代码人',
              link: 'https://bugcoder.asia/',
              avatar: 'https://bugcoder.asia/headLogo.jpg',
              descr: '没有比这再简陋的网站了'
            },
            {
              name: '鈴奈咲桜のBlog',
              link: 'https://blog.sakura.ink',
              avatar: 'https://q2.qlogo.cn/headimg_dl?dst_uin=2731443459&spec=5',
              descr: '愛することを忘れないで'
            },
            {
              name: 'LYEy_isine个人博客',
              link: 'https://caiyifeng.top',
              avatar: 'https://caiyifeng.top/avatar.jpg',
              descr: '花海无一日,少年踏自来'
            },
            {
              name: '安小歪',
              link: 'https://blog.anxy.top/',
              avatar: 'https://q2.qlogo.cn/headimg_dl?dst_uin=2622979530&spec=640',
              descr: '记住你！自己！'
            },
            {
              name: '浪小舟的博客',
              link: 'https://blog.lonzov.top/',
              avatar: 'https://img.fastmirror.net/s/2025/09/12/68c39893a84aa.png',
              descr: '向利而生，随心而活'
            },
            {
              name: 'vegecaiBlog',
              link: 'https://blog.vegecai.moe/',
              avatar: 'https://img.vegecai.moe/me.jpg',
              descr: '守岁花开，季季不同'
            },
            {
              name: '东方月初',
              link: 'https://blog.biuxin.com/',
              avatar: 'https://blog.ayeez.cn/imgs/icon/biuxin.webp',
              descr: '分享有趣但又无聊的东西。'
            },
            {
              name: '栖童の小站',
              link: 'https://blog.linux-qitong.top',
              avatar: 'https://blog.ayeez.cn/imgs/icon/qitong.png',
              descr: '越努力,越幸运'
            },
            {
              name: 'InsectMk',
              link: 'https://insectmk.cn',
              avatar: 'https://insectmk.cn/static/img/head/insectmk.jpg',
              descr: '每天都要微笑'
            },
            {
              name: 'kzhik\'s website',
              link: 'https://www.kzhik.cn',
              avatar: 'https://www.kzhik.cn/avatar.webp',
              descr: 'EXPLORE THE WORLD!'
            },
            {
              name: 'Nebula Blog',
              link: 'https://www.996icu.eu.org/',
              avatar: 'https://img.scdn.io/i/692d847f79589_1764590719.webp',
              descr: 'Nebula.SYS'
            },
            {
              name: '威威一笑音乐网',
              link: 'https://music.weismile.top/',
              avatar: 'https://music.weismile.top/wp-content/uploads/2025/12/20251219150130663767.webp',
              descr: '一个免费的音乐资源分享网站'
            },
            {
              name: '江鸟博客',
              link: 'https://blog.azucat.eu',
              avatar: 'https://blog.azucat.eu/favicon/icon.png',
              descr: '天有不公~地有苍生~万般万般年月过~落日朱门满地红~'
            },
            {
              name: 'SAKURAIN TEAM',
              link: 'https://sakurain.net/',
              avatar: 'https://sakurain.net/image/logo.webp',
              descr: '用代码构建未来'
            },
            {
              name: 'Inalineの小站',
              link: 'https://inaline.net',
              avatar: 'https://inaline.net/usr/themes/inaline/assets/images/logo/cover.png',
              descr: '此情可待成追忆，只是当时已惘然'
            },
            {
              name: '时光潜流',
              link: 'https://www.dreamcenter.top',
              descr: '妹控的中二君！',
              avatar: 'https://www.dreamcenter.top/imgs/avatar.jpg'
            }
          ]
        }
      ]
    };
  },
  mounted() {
    // Twikoo 的“同一页面”是由 path 决定的；要复用旧站评论，需要把这里的 path 设置成旧站当时友链页的 path
    const envId = 'https://twikoo.ayeez.cn';
    const oldSiteLinksPath = '/link';

    if (!window.twikoo) return;
    this.$nextTick(() => {
      window.twikoo.init({
        envId,
        el: '#tlinkcomment',
        path: oldSiteLinksPath
      });
    });
  }
};
</script>

<style scoped>
.links {
  padding: 20px;
  width: 100%;
  box-sizing: border-box;
  color: white;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
}

.links-inner {
  width: 100%;
  max-width: 1300px;
}

.links-header {
  font-family: 'Bebas Neue', Arial, sans-serif;
  margin-bottom: 8px;
}

.links-tagline {
  font-size: 20px;
  letter-spacing: 4px;
  color: #cccccc;
}

.links-title-gradient {
  font-size: 46px;
  letter-spacing: 6px;
  background-image: linear-gradient(to right, #abe6a8, #00b828);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.links-subtitle {
  margin-bottom: 24px;
  color: #cccccc;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.link-card {
  position: relative;
  text-decoration: none;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 16px;
  border-radius: 10px;
  background: linear-gradient(135deg,
      rgba(108, 171, 106, 0.3),
      rgba(42, 184, 73, 0.6));
  border: 2px solid #ffffff00;
  box-shadow:
    inset 0 0 10px rgba(255, 255, 255, 0.15),
    0 4px 15px rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  overflow: hidden;
  transition: transform 0.3s ease, border 0.3s ease, box-shadow 0.3s ease;
}

.link-card::before {
  content: '';
  position: absolute;
  top: -100%;
  left: 0;
  width: 100%;
  height: 5px;
  background: linear-gradient(to bottom, transparent, #00b828, transparent);
  z-index: 1;
  opacity: 0;
}

.link-card:hover {
  transform: translateY(-5px);
  border: 2px solid #00b828;
  box-shadow: 0 6px 15px rgba(0, 184, 40, 0.4);
}

.link-card:hover::before {
  opacity: 1;
  animation: scanLine 1s ease-in-out;
}

.link-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  /* margin-bottom: 12px; */
}

.link-avatar {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.6);
}

.link-meta {
  overflow: hidden;
}

.link-name {
  color: #ffffff;
  font-size: 1.1rem;
  margin: 0 0 4px 0;
}

.link-descr {
  margin: 0;
  font-size: 0.9rem;
  color: #cccccc;
  white-space: normal;
  text-overflow: clip;
  overflow: visible;
}

.links-rules {
  margin-top: 24px;
  margin-bottom: 24px;
  padding: 16px 18px;
  background-color: rgba(0, 0, 0, 0.8);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  font-size: 0.95rem;
}

.rules-title {
  margin-top: 0;
  margin-bottom: 6px;
  font-size: 1.2rem;
  color: #ffffff;
}

.rules-subtitle {
  margin: 4px 0;
  color: #dddddd;
}

.rules-subtitle-strong {
  margin: 10px 0 4px;
  font-size: 1.05rem;
  color: #ffffff;
}

.links-rules ul {
  padding-left: 1.2em;
  margin: 8px 0 16px;
}

.links-rules li {
  margin: 4px 0;
}

.highlight {
  color: #7af58f;
  font-weight: 600;
}

@keyframes scanLine {
  0% {
    top: -10px;
  }

  100% {
    top: 100%;
  }
}

.links-code {
  background-color: #111;
  border-radius: 6px;
  padding: 10px 12px;
  font-family: 'Courier New', monospace;
  font-size: 0.85rem;
  white-space: pre;
  overflow-x: auto;
  border: 1px solid rgba(255, 255, 255, 0.3);
  margin-bottom: 12px;
}

.links-tip {
  margin-top: 4px;
  color: #cccccc;
}

.comment-card {
  background-color: rgba(0, 0, 0, 0.8);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.5);
  margin-top: 24px;
}

.comment-title {
  margin-top: 0;
  margin-bottom: 10px;
}

#tlinkcomment {
  color: white;
}
</style>