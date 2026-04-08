<template>
  <div class="albums-manage-page page-card">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="相册管理" name="album">
        <div class="page-toolbar">
          <el-button type="primary" @click="loadAlbums">刷新</el-button>
          <el-button type="success" @click="openAlbumAddDialog">新增相册</el-button>
        </div>

        <div class="table-wrap">
          <el-table :data="albumList" stripe style="width: 100%;">
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column prop="title" label="相册标题" min-width="200" />
            <el-table-column prop="description" label="描述" min-width="260" />
            <el-table-column prop="sort" label="排序" width="90" />
            <el-table-column label="操作" width="280">
              <template #default="scope">
                <div class="row-actions">
                  <el-button size="small" type="primary" @click="openAlbumEditDialog(scope.row)">编辑</el-button>
                  <el-button size="small" @click="openPhotoTab(scope.row)">管理图片</el-button>
                  <el-button size="small" type="danger" @click="removeAlbum(scope.row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <el-tab-pane label="图片管理" name="photo">
        <div class="page-toolbar">
          <el-select v-model="selectedAlbumId" placeholder="请选择相册" class="toolbar-select" @change="loadPhotos">
            <el-option v-for="a in albumList" :key="a.id" :label="a.title" :value="a.id" />
          </el-select>
          <el-button type="primary" @click="loadPhotos" :disabled="!selectedAlbumId">刷新</el-button>
          <el-button type="success" @click="openPhotoAddDialog" :disabled="!selectedAlbumId">新增图片</el-button>
        </div>

        <div class="table-wrap">
          <el-table :data="photoList" stripe style="width: 100%;">
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column label="预览" width="100">
              <template #default="scope">
                <el-image
                  v-if="scope.row.imageUrl"
                  :src="scope.row.imageUrl"
                  fit="cover"
                  style="width: 48px; height: 48px; border-radius: 4px;"
                  :preview-src-list="[scope.row.imageUrl]"
                  preview-teleported
                />
              </template>
            </el-table-column>
            <el-table-column prop="caption" label="文案" min-width="240" />
            <el-table-column prop="sort" label="排序" width="90" />
            <el-table-column label="操作" width="180">
              <template #default="scope">
                <div class="row-actions">
                  <el-button size="small" type="primary" @click="openPhotoEditDialog(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="removePhoto(scope.row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="isAlbumEdit ? '编辑相册' : '新增相册'" v-model="albumDialogVisible" width="520px">
      <el-form :model="albumForm" label-width="90px">
        <el-form-item label="标题">
          <el-input v-model="albumForm.title" placeholder="请输入相册标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="albumForm.description" type="textarea" :rows="3" placeholder="请输入相册描述（可选）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="albumForm.sort" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="albumDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAlbumForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="isPhotoEdit ? '编辑图片' : '新增图片'" v-model="photoDialogVisible" width="640px">
      <el-form :model="photoForm" label-width="90px">
        <el-form-item label="所属相册">
          <el-select v-model="photoForm.albumId" placeholder="请选择相册" style="width: 100%;">
            <el-option v-for="a in albumList" :key="a.id" :label="a.title" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="photoForm.imageUrl" placeholder="请输入图片URL" />
        </el-form-item>
        <el-form-item label="文案">
          <el-input v-model="photoForm.caption" placeholder="请输入文案（可选）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="photoForm.sort" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="photoDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPhotoForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  addAlbum,
  addAlbumPhoto,
  deleteAlbum,
  deleteAlbumPhoto,
  getAlbumList,
  getAlbumPhotoList,
  updateAlbum,
  updateAlbumPhoto
} from '@/api'

export default {
  data() {
    return {
      activeTab: 'album',
      albumList: [],
      selectedAlbumId: null,
      photoList: [],
      albumDialogVisible: false,
      photoDialogVisible: false,
      isAlbumEdit: false,
      isPhotoEdit: false,
      albumForm: {
        id: null,
        title: '',
        description: '',
        sort: 0
      },
      photoForm: {
        id: null,
        albumId: null,
        imageUrl: '',
        caption: '',
        sort: 0
      }
    }
  },
  mounted() {
    this.loadAlbums()
  },
  methods: {
    async loadAlbums() {
      try {
        const data = await getAlbumList()
        this.albumList = data || []
        if (!this.selectedAlbumId && this.albumList.length) {
          this.selectedAlbumId = this.albumList[0].id
        }
      } catch (error) {
        console.error('获取相册列表失败:', error)
        this.$message.error('获取相册列表失败')
      }
    },
    async loadPhotos() {
      if (!this.selectedAlbumId) {
        this.photoList = []
        return
      }
      try {
        const data = await getAlbumPhotoList({ albumId: this.selectedAlbumId })
        this.photoList = data || []
      } catch (error) {
        console.error('获取相册图片失败:', error)
        this.$message.error('获取相册图片失败')
      }
    },
    openPhotoTab(album) {
      this.activeTab = 'photo'
      this.selectedAlbumId = album.id
      this.loadPhotos()
    },
    openAlbumAddDialog() {
      this.isAlbumEdit = false
      this.albumForm = { id: null, title: '', description: '', sort: 0 }
      this.albumDialogVisible = true
    },
    openAlbumEditDialog(row) {
      this.isAlbumEdit = true
      this.albumForm = {
        id: row.id,
        title: row.title || '',
        description: row.description || '',
        sort: row.sort ?? 0
      }
      this.albumDialogVisible = true
    },
    async submitAlbumForm() {
      if (!this.albumForm.title || !this.albumForm.title.trim()) {
        this.$message.warning('请输入相册标题')
        return
      }
      const payload = {
        id: this.albumForm.id,
        title: this.albumForm.title.trim(),
        description: this.albumForm.description,
        sort: this.albumForm.sort
      }
      try {
        if (this.isAlbumEdit) {
          await updateAlbum(payload)
          this.$message.success('相册更新成功')
        } else {
          await addAlbum(payload)
          this.$message.success('相册新增成功')
        }
        this.albumDialogVisible = false
        await this.loadAlbums()
      } catch (error) {
        console.error('保存相册失败:', error)
        this.$message.error('保存相册失败')
      }
    },
    removeAlbum(row) {
      this.$confirm(`确定删除相册 "${row.title}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(async () => {
          await deleteAlbum({ id: row.id })
          this.$message.success('删除成功')
          if (this.selectedAlbumId === row.id) {
            this.selectedAlbumId = null
            this.photoList = []
          }
          await this.loadAlbums()
        })
        .catch(() => {})
    },
    openPhotoAddDialog() {
      this.isPhotoEdit = false
      this.photoForm = {
        id: null,
        albumId: this.selectedAlbumId || null,
        imageUrl: '',
        caption: '',
        sort: 0
      }
      this.photoDialogVisible = true
    },
    openPhotoEditDialog(row) {
      this.isPhotoEdit = true
      this.photoForm = {
        id: row.id,
        albumId: row.albumId,
        imageUrl: row.imageUrl || '',
        caption: row.caption || '',
        sort: row.sort ?? 0
      }
      this.photoDialogVisible = true
    },
    async submitPhotoForm() {
      if (!this.photoForm.albumId) {
        this.$message.warning('请选择相册')
        return
      }
      if (!this.photoForm.imageUrl || !this.photoForm.imageUrl.trim()) {
        this.$message.warning('请输入图片URL')
        return
      }
      const payload = {
        id: this.photoForm.id,
        albumId: this.photoForm.albumId,
        imageUrl: this.photoForm.imageUrl.trim(),
        caption: this.photoForm.caption,
        sort: this.photoForm.sort
      }
      try {
        if (this.isPhotoEdit) {
          await updateAlbumPhoto(payload)
          this.$message.success('图片更新成功')
        } else {
          await addAlbumPhoto(payload)
          this.$message.success('图片新增成功')
        }
        this.photoDialogVisible = false
        this.selectedAlbumId = payload.albumId
        await this.loadPhotos()
      } catch (error) {
        console.error('保存图片失败:', error)
        this.$message.error('保存图片失败')
      }
    },
    removePhoto(row) {
      this.$confirm('确定删除该图片吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(async () => {
          await deleteAlbumPhoto({ id: row.id })
          this.$message.success('删除成功')
          await this.loadPhotos()
        })
        .catch(() => {})
    }
  }
}
</script>

<style scoped>
.albums-manage-page {
  padding: 16px;
}

.toolbar-select {
  width: 240px;
}

@media (max-width: 768px) {
  .toolbar-select {
    width: 100%;
  }
}
</style>
