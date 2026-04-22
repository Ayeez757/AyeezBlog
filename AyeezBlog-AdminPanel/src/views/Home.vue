<template>
  <div class="dashboard">
    <div class="dashboard-head">
      <div class="head-left">
        <h2 class="head-title">管理控制台</h2>
        <div class="head-sub">访问流量（PV/UV） + 最近评论</div>
      </div>

      <div class="head-right">
        <el-date-picker
          v-model="dateRange"
          size="small"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          format="YYYY-MM-DD"
          unlink-panels
          :shortcuts="rangeShortcuts"
          class="date-range"
          @change="loadDashboard"
        />
        <el-button type="primary" size="small" :loading="loadingDashboard" @click="loadDashboard">
          刷新流量
        </el-button>
      </div>
    </div>

    <div class="metric-row">
      <el-card shadow="never" class="metric-card">
        <div class="metric-label">总访问量（PV）</div>
        <div class="metric-value">{{ pageViews || 0 }}</div>
        <div class="metric-extra">今日 PV：{{ todayPoint.pageViews }}</div>
      </el-card>

      <el-card shadow="never" class="metric-card">
        <div class="metric-label">总访客量（UV）</div>
        <div class="metric-value">{{ uniqueVisitors || 0 }}</div>
        <div class="metric-extra">今日 UV：{{ todayPoint.uniqueVisitors }}</div>
      </el-card>
    </div>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12" :xs="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span>累计 PV/UV（可叠加）</span>
          </template>
          <div class="chart-controls">
            <el-checkbox-group v-model="legendSelectedTotal" @change="updateTotalLegend">
              <el-checkbox label="累计 PV" />
              <el-checkbox label="累计 UV" />
              <el-checkbox label="PV（日）" />
              <el-checkbox label="UV（日）" />
            </el-checkbox-group>
          </div>
          <div ref="totalChartRef" class="chart"></div>
        </el-card>
      </el-col>

      <el-col :span="12" :xs="24">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <span>每日 PV/UV（可叠加）</span>
          </template>
          <div class="chart-controls">
            <el-checkbox-group v-model="legendSelectedDaily" @change="updateDailyLegend">
              <el-checkbox label="PV（日）" />
              <el-checkbox label="UV（日）" />
              <el-checkbox label="累计 PV" />
              <el-checkbox label="累计 UV" />
            </el-checkbox-group>
          </div>
          <div ref="dailyChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="comments-card">
      <template #header>
        <div class="comments-head">
          <span>最新评论列表</span>
          <div class="head-actions">
            <el-button type="text" size="small" :loading="loadingComments" @click="loadLatestComments">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="loadingComments" class="loading-wrap">
        <el-skeleton :rows="8" animated />
      </div>

      <el-table v-else :data="latestComments" height="320" style="width: 100%;">
        <el-table-column prop="nick" label="昵称" width="130" />
        <el-table-column prop="timeText" label="时间" width="190" />
        <el-table-column prop="page" label="页面" width="180" />
        <el-table-column label="内容">
          <template #default="scope">
            <div class="comment-text" :title="scope.row.text">
              {{ scope.row.text }}
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loadingComments && !latestComments.length" class="empty-tips">
        暂无最新评论数据
      </div>
    </el-card>
  </div>
</template>

<script>
import { getAdminDashboardStats, getPostList } from '@/api'

const PROD_TWIKOO_URL = 'https://twikoo.ayeez.cn'
const TWIKOO_HOSTNAME = (() => {
  try {
    return new URL(PROD_TWIKOO_URL).hostname
  } catch {
    return 'twikoo.ayeez.cn'
  }
})()

const pad2 = (n) => (n < 10 ? `0${n}` : `${n}`)
const formatYmd = (d) => {
  const x = d instanceof Date ? d : new Date(d)
  if (Number.isNaN(x.getTime())) return ''
  return `${x.getFullYear()}-${pad2(x.getMonth() + 1)}-${pad2(x.getDate())}`
}
const lastNDaysRange = (n) => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - (n - 1))
  return [formatYmd(start), formatYmd(end)]
}

export default {
  name: 'Home',
  data() {
    return {
      dateRange: [],

      loadingDashboard: false,
      loadingComments: false,

      pageViews: 0,
      uniqueVisitors: 0,
      history: [],

      totalChart: null,
      dailyChart: null,
      // 图表图例选中状态（用于手动控制叠加）
      legendSelectedTotal: ['累计 PV', '累计 UV'],
      legendSelectedDaily: ['PV（日）', 'UV（日）'],

      latestComments: [],

      rangeShortcuts: [
        {
          text: '最近 7 天',
          value: () => lastNDaysRange(7),
        },
        {
          text: '最近 14 天',
          value: () => lastNDaysRange(14),
        },
        {
          text: '最近 30 天',
          value: () => lastNDaysRange(30),
        },
      ],
    }
  },
  computed: {
    todayPoint() {
      const list = Array.isArray(this.history) ? this.history : []
      if (!list.length) return { pageViews: 0, uniqueVisitors: 0 }
      const last = list[list.length - 1] || {}
      return {
        pageViews: last.pageViews || 0,
        uniqueVisitors: last.uniqueVisitors || 0,
      }
    },
  },
  mounted() {
    // 默认：最近 14 天
    this.dateRange = lastNDaysRange(14)
    this.loadDashboard()
    this.loadLatestComments()
    window.addEventListener('resize', this.handleResize)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.handleResize)
    if (this.totalChart) this.totalChart.dispose()
    if (this.dailyChart) this.dailyChart.dispose()
  },
  methods: {
    async loadDashboard() {
      this.loadingDashboard = true
      try {
        const [startDate, endDate] = Array.isArray(this.dateRange) ? this.dateRange : []
        const params =
          startDate && endDate
            ? { startDate, endDate }
            : { days: 14 }
        const data = await getAdminDashboardStats(params)
        this.pageViews = data?.pageViews || 0
        this.uniqueVisitors = data?.uniqueVisitors || 0
        this.history = Array.isArray(data?.history) ? data.history : []
        this.renderCharts()
      } catch (e) {
        console.error('加载仪表盘流量失败:', e)
        this.$message.error('加载流量数据失败')
      } finally {
        this.loadingDashboard = false
      }
    },

    renderCharts() {
      if (!window.echarts) return
      if (!Array.isArray(this.history) || !this.history.length) return

      const totalEl = this.$refs.totalChartRef
      const dailyEl = this.$refs.dailyChartRef
      if (!totalEl || !dailyEl) return

      if (!this.totalChart) this.totalChart = window.echarts.init(totalEl)
      if (!this.dailyChart) this.dailyChart = window.echarts.init(dailyEl)

      const xData = this.history.map((i) => i.date)
      const pvData = this.history.map((i) => i.pageViews || 0)
      const uvData = this.history.map((i) => i.uniqueVisitors || 0)

      // 在前端把“日新增”转换成“累计总量趋势”
      const pvTotalData = []
      const uvTotalData = []
      let pvSum = 0
      let uvSum = 0
      for (let i = 0; i < this.history.length; i++) {
        pvSum += Number(pvData[i] || 0)
        uvSum += Number(uvData[i] || 0)
        pvTotalData.push(pvSum)
        uvTotalData.push(uvSum)
      }

      const baseLegend = {
        show: true,
        type: 'scroll',
        orient: 'horizontal',
        top: 6,
        data: ['PV（日）', 'UV（日）', '累计 PV', '累计 UV'],
        // 曲线显隐由外部 checkbox 控制；禁用 legend 点击，避免交互偶发失效
        selectedMode: false,
      }

      const baseCommon = {
        tooltip: { trigger: 'axis' },
        grid: { left: 10, right: 10, bottom: 0, top: 60, containLabel: true },
        xAxis: { type: 'category', data: xData, boundaryGap: false, axisLabel: { color: '#666' } },
        yAxis: { type: 'value', axisLabel: { color: '#666' } },
        series: [
          {
            name: 'PV（日）',
            type: 'line',
            data: pvData,
            smooth: true,
            lineStyle: { width: 2 },
            itemStyle: { color: '#22c55e' },
            areaStyle: { opacity: 0.03 },
          },
          {
            name: 'UV（日）',
            type: 'line',
            data: uvData,
            smooth: true,
            lineStyle: { width: 2 },
            itemStyle: { color: '#3b82f6' },
            areaStyle: { opacity: 0.03 },
          },
          {
            name: '累计 PV',
            type: 'line',
            data: pvTotalData,
            smooth: true,
            lineStyle: { width: 2 },
            itemStyle: { color: '#16a34a' },
            areaStyle: { opacity: 0.08 },
          },
          {
            name: '累计 UV',
            type: 'line',
            data: uvTotalData,
            smooth: true,
            lineStyle: { width: 2 },
            itemStyle: { color: '#1d4ed8' },
            areaStyle: { opacity: 0.08 },
          },
        ],
      }

      // 默认：累计图叠加累计两条；每日图叠加每日两条
      const totalOption = {
        ...baseCommon,
        legend: {
          ...baseLegend,
          selected: this.buildLegendSelected(this.legendSelectedTotal),
        },
      }

      const dailyOption = {
        ...baseCommon,
        legend: {
          ...baseLegend,
          selected: this.buildLegendSelected(this.legendSelectedDaily),
        },
      }

      this.totalChart.setOption(totalOption)
      this.dailyChart.setOption(dailyOption)
    },

    buildLegendSelected(list) {
      const names = ['PV（日）', 'UV（日）', '累计 PV', '累计 UV']
      const selected = {}
      names.forEach((n) => {
        selected[n] = Array.isArray(list) ? list.includes(n) : false
      })
      return selected
    },

    updateTotalLegend() {
      if (!this.totalChart) return
      const selected = this.buildLegendSelected(this.legendSelectedTotal)
      this.totalChart.setOption({ legend: { selected } })
    },

    updateDailyLegend() {
      if (!this.dailyChart) return
      const selected = this.buildLegendSelected(this.legendSelectedDaily)
      this.dailyChart.setOption({ legend: { selected } })
    },

    handleResize() {
      try {
        if (this.totalChart) this.totalChart.resize()
        if (this.dailyChart) this.dailyChart.resize()
      } catch (_) {}
    },

    getTwikooEnvId() {
      if (typeof window === 'undefined') return PROD_TWIKOO_URL
      const host = window.location.hostname
      if (host === TWIKOO_HOSTNAME) return PROD_TWIKOO_URL
      return `${window.location.origin}/twikoo-proxy/`
    },

    htmlToText(html) {
      const s = (html || '').toString()
      if (!s) return ''
      if (typeof document === 'undefined') return s
      const el = document.createElement('div')
      el.innerHTML = s
      return (el.innerText || el.textContent || '').trim()
    },

    formatTime(created) {
      const ts = typeof created === 'number' ? created : Number(created)
      if (!ts || Number.isNaN(ts)) return ''
      const d = new Date(ts)
      if (Number.isNaN(d.getTime())) return ''
      return d.toLocaleString('zh-CN', { hour12: false })
    },

    mapCommentPathToPagePath(raw) {
      const s = (raw || '').toString()
      if (!s) return s
      // 友链页：Twikoo 历史评论聚合在 /link，但实际页面是 /links
      if (s === '/link') return '/links'
      return s
    },

    twikooKeysForUrl(rawUrl) {
      const pagePath = this.mapCommentPathToPagePath(rawUrl)
      const origin = window.location.origin
      const raw = (rawUrl || '').toString()

      const keys = new Set()
      if (raw) keys.add(raw)
      if (pagePath && pagePath !== raw) keys.add(pagePath)
      const rawIsAbs = raw.startsWith('http://') || raw.startsWith('https://')
      const pagePathIsAbs = pagePath && (pagePath.startsWith('http://') || pagePath.startsWith('https://'))
      if (raw && !rawIsAbs) keys.add(`${origin}${raw}`)
      if (pagePath && pagePath !== raw && !pagePathIsAbs) keys.add(`${origin}${pagePath}`)

      return Array.from(keys).filter(Boolean)
    },

    async twikooCommentGet({ url, before }) {
      const payload = { event: 'COMMENT_GET', url }
      if (before) payload.before = before
      const envId = this.getTwikooEnvId()

      const r = await fetch(envId, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify(payload),
      })

      if (!r.ok) {
        let detail = ''
        try {
          detail = await r.text()
        } catch (_) {}
        throw new Error(`Twikoo 请求失败：${r.status}${detail ? ` ${detail}` : ''}`)
      }
      return await r.json()
    },

    async fetchTopCommentsForUrl(rawUrl, limitPerUrl = 3) {
      const keys = this.twikooKeysForUrl(rawUrl)
      const dedup = new Map()

      for (const k of keys) {
        if (dedup.size >= limitPerUrl) break
        const res = await this.twikooCommentGet({ url: k })
        const data = Array.isArray(res?.data) ? res.data : []
        for (const c of data) {
          if (!c || !c.id) continue
          if (dedup.has(c.id)) continue

          const textRaw = c.commentText ?? c.comment ?? c.text ?? ''
          const text = this.htmlToText(textRaw)
          dedup.set(c.id, {
            id: c.id,
            nick: c.nick || '匿名',
            created: c.created,
            page: this.mapCommentPathToPagePath(rawUrl),
            text,
          })
        }
      }

      return Array.from(dedup.values()).slice(0, limitPerUrl)
    },

    async loadLatestComments() {
      this.loadingComments = true
      this.latestComments = []
      try {
        const resp = await getPostList({ page: 1, pageSize: 20, orderBy: 'update_time', orderType: 'desc' })
        const posts = resp?.rows || []

        // 为了避免拉全站所有页面，这里取“最新评论更可能出现的页面”：
        // 1) 留言页 /comments、友链页 /link
        // 2) 最近更新的若干篇文章（取前 8 篇）
        const urlKeys = ['/comments', '/link', ...posts.slice(0, 8).map((p) => `/posts/${p.id}`)]

        const out = []
        const concurrency = 3
        let idx = 0

        const worker = async () => {
          while (idx < urlKeys.length) {
            const u = urlKeys[idx++]
            try {
              const items = await this.fetchTopCommentsForUrl(u, 3)
              out.push(...items)
            } catch (e) {
              console.warn('拉取评论失败:', u, e)
            }
          }
        }

        const workers = Array.from({ length: Math.min(concurrency, urlKeys.length) }, () => worker())
        await Promise.all(workers)

        const dedup = new Map()
        for (const c of out) {
          if (!c?.id) continue
          dedup.set(c.id, c)
        }

        const list = Array.from(dedup.values())
          .sort((a, b) => (Number(b.created) || 0) - (Number(a.created) || 0))
          .slice(0, 10)
          .map((x) => ({
            ...x,
            timeText: this.formatTime(x.created),
            text: x.text || '',
          }))

        this.latestComments = list
      } catch (e) {
        console.error('加载最新评论失败:', e)
        this.$message.error('加载最新评论失败')
      } finally {
        this.loadingComments = false
      }
    },
  },
}
</script>

<style scoped>
.dashboard {
  padding: 16px;
}

.dashboard-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.head-title {
  margin: 0;
  font-size: 18px;
}

.head-sub {
  color: #6b7280;
  font-size: 13px;
  margin-top: 4px;
}

.head-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.date-range {
  width: 280px;
}

.metric-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.metric-card {
  min-height: 110px;
}

.metric-label {
  color: #6b7280;
  font-size: 13px;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #111827;
}

.metric-extra {
  margin-top: 8px;
  color: #6b7280;
  font-size: 13px;
}

.chart-row {
  margin-bottom: 16px;
}

.chart {
  height: 320px;
  width: 100%;
}

.chart-controls {
  margin-bottom: 8px;
  font-size: 12px;
}

.comments-card {
  margin-top: 16px;
}

.comments-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
}

.comment-text {
  max-width: 560px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-tips {
  padding: 12px 0 0;
  color: #9ca3af;
  font-size: 13px;
  text-align: center;
}

.loading-wrap {
  padding: 6px 0;
}

@media (max-width: 768px) {
  .metric-row {
    grid-template-columns: 1fr;
  }
  .chart {
    height: 260px;
  }
  .chart-row {
    margin-bottom: 12px;
  }

  :deep(.chart-controls .el-checkbox-group) {
    display: flex;
    flex-wrap: wrap;
    gap: 6px 12px;
  }
}
</style>