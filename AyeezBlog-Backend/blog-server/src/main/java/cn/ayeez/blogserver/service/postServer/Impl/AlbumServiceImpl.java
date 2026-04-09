package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.response.AlbumDetail;
import cn.ayeez.blogpojo.dto.response.AlbumSummary;
import cn.ayeez.blogpojo.entity.Album;
import cn.ayeez.blogpojo.entity.AlbumPhoto;
import cn.ayeez.blogserver.mapper.AlbumMapper;
import cn.ayeez.blogserver.service.postServer.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlbumServiceImpl implements AlbumService {

    @Autowired
    private AlbumMapper albumMapper;

    @Override
    public List<AlbumSummary> listPublicAlbums() {
        List<Album> albums = albumMapper.listAlbums();
        if (albums == null || albums.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> albumIds = new ArrayList<>();
        for (Album album : albums) {
            albumIds.add(album.getId());
        }
        List<AlbumPhoto> covers = albumMapper.listPhotosByAlbumIds(albumIds, 3);
        Map<Long, List<String>> coverMap = new LinkedHashMap<>();
        for (AlbumPhoto photo : covers) {
            coverMap.computeIfAbsent(photo.getAlbumId(), k -> new ArrayList<>()).add(photo.getImageUrl());
        }

        List<AlbumSummary> result = new ArrayList<>();
        for (Album album : albums) {
            AlbumSummary row = new AlbumSummary();
            row.setId(album.getId());
            row.setTitle(album.getTitle());
            row.setDescription(album.getDescription());
            row.setSort(album.getSort());
            row.setDefaultCoverSource(album.getDefaultCoverSource());
            row.setCoverImages(coverMap.getOrDefault(album.getId(), new ArrayList<>()));
            row.setPhotoCount(albumMapper.countPhotosByAlbumId(album.getId()));
            result.add(row);
        }
        return result;
    }

    @Override
    public AlbumDetail getPublicAlbumDetail(Long id) {
        if (id == null) return null;
        Album album = albumMapper.getAlbumById(id);
        if (album == null) return null;
        List<AlbumPhoto> photos = albumMapper.listPhotosByAlbumId(id);

        AlbumDetail detail = new AlbumDetail();
        detail.setId(album.getId());
        detail.setTitle(album.getTitle());
        detail.setDescription(album.getDescription());
        detail.setSort(album.getSort());
        detail.setPhotos(photos != null ? photos : Collections.emptyList());

        List<String> coverImages = new ArrayList<>();
        if (photos != null) {
            for (int i = 0; i < photos.size() && i < 3; i++) {
                coverImages.add(photos.get(i).getImageUrl());
            }
        }
        detail.setCoverImages(coverImages);
        return detail;
    }

    @Override
    public List<Album> listAdminAlbums() {
        List<Album> albums = albumMapper.listAlbums();
        return albums != null ? albums : new ArrayList<>();
    }

    @Override
    @Transactional
    public Result addAlbum(Album album) {
        Result validate = validateAlbum(album, false);
        if (validate != null) return validate;
        Integer maxSort = albumMapper.selectMaxAlbumSort();
        album.setSort((maxSort == null || maxSort < 0) ? 0 : maxSort + 1);
        if (album.getDefaultCoverSource() == null) {
            album.setDefaultCoverSource(0);
        }
        albumMapper.insertAlbum(album);
        if (album.getDefaultCoverSource() != null && album.getDefaultCoverSource() == 1) {
            albumMapper.clearDefaultCoverSource();
            albumMapper.markAlbumAsDefaultCoverSource(album.getId());
        }
        return Result.success();
    }

    @Override
    @Transactional
    public Result updateAlbum(Album album) {
        Result validate = validateAlbum(album, true);
        if (validate != null) return validate;
        if (album.getSort() == null) {
            album.setSort(0);
        }
        if (album.getDefaultCoverSource() == null) {
            album.setDefaultCoverSource(0);
        }
        albumMapper.updateAlbum(album);
        if (album.getDefaultCoverSource() != null && album.getDefaultCoverSource() == 1) {
            albumMapper.clearDefaultCoverSource();
            albumMapper.markAlbumAsDefaultCoverSource(album.getId());
        }
        return Result.success();
    }

    @Override
    @Transactional
    public Result deleteAlbum(Long id) {
        if (id == null) return Result.error(400, "相册ID不能为空");
        albumMapper.deleteAlbumById(id);
        return Result.success();
    }

    @Override
    @Transactional
    public Result setDefaultCoverAlbum(Long id) {
        if (id == null) return Result.error(400, "相册ID不能为空");
        Album target = albumMapper.getAlbumById(id);
        if (target == null) return Result.error(404, "相册不存在");
        albumMapper.clearDefaultCoverSource();
        int affected = albumMapper.markAlbumAsDefaultCoverSource(id);
        if (affected <= 0) return Result.error(500, "设置默认封面相册失败");
        return Result.success();
    }

    @Override
    public List<AlbumPhoto> listAdminPhotos(Long albumId) {
        if (albumId == null) return new ArrayList<>();
        List<AlbumPhoto> photos = albumMapper.listPhotosByAlbumId(albumId);
        return photos != null ? photos : new ArrayList<>();
    }

    @Override
    @Transactional
    public Result addPhoto(AlbumPhoto photo) {
        Result validate = validatePhoto(photo, false);
        if (validate != null) return validate;
        Integer maxSort = albumMapper.selectMaxPhotoSort(photo.getAlbumId());
        photo.setSort((maxSort == null || maxSort < 0) ? 0 : maxSort + 1);
        albumMapper.insertPhoto(photo);
        return Result.success();
    }

    @Override
    @Transactional
    public Result updatePhoto(AlbumPhoto photo) {
        Result validate = validatePhoto(photo, true);
        if (validate != null) return validate;
        if (photo.getSort() == null) {
            photo.setSort(0);
        }
        albumMapper.updatePhoto(photo);
        return Result.success();
    }

    @Override
    @Transactional
    public Result deletePhoto(Long id) {
        if (id == null) return Result.error(400, "图片ID不能为空");
        albumMapper.deletePhotoById(id);
        return Result.success();
    }

    private Result validateAlbum(Album album, boolean requireId) {
        if (album == null) return Result.error(400, "参数不能为空");
        if (requireId && album.getId() == null) return Result.error(400, "相册ID不能为空");
        if (album.getTitle() == null || album.getTitle().trim().isEmpty()) return Result.error(400, "相册标题不能为空");
        album.setTitle(album.getTitle().trim());
        if (album.getDescription() != null) {
            album.setDescription(album.getDescription().trim());
            if (album.getDescription().isEmpty()) album.setDescription(null);
        }
        return null;
    }

    private Result validatePhoto(AlbumPhoto photo, boolean requireId) {
        if (photo == null) return Result.error(400, "参数不能为空");
        if (requireId && photo.getId() == null) return Result.error(400, "图片ID不能为空");
        if (photo.getAlbumId() == null) return Result.error(400, "相册ID不能为空");
        if (photo.getImageUrl() == null || photo.getImageUrl().trim().isEmpty()) return Result.error(400, "图片地址不能为空");
        photo.setImageUrl(photo.getImageUrl().trim());
        if (photo.getCaption() != null) {
            photo.setCaption(photo.getCaption().trim());
            if (photo.getCaption().isEmpty()) photo.setCaption(null);
        }
        return null;
    }
}
