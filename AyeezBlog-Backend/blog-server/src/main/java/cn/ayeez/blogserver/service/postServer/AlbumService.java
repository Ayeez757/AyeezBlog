package cn.ayeez.blogserver.service.postServer;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.response.AlbumDetail;
import cn.ayeez.blogpojo.dto.response.AlbumSummary;
import cn.ayeez.blogpojo.entity.Album;
import cn.ayeez.blogpojo.entity.AlbumPhoto;

import java.util.List;

public interface AlbumService {
    List<AlbumSummary> listPublicAlbums();

    AlbumDetail getPublicAlbumDetail(Long id);

    List<Album> listAdminAlbums();

    Result addAlbum(Album album);

    Result updateAlbum(Album album);

    Result deleteAlbum(Long id);

    List<AlbumPhoto> listAdminPhotos(Long albumId);

    Result addPhoto(AlbumPhoto photo);

    Result updatePhoto(AlbumPhoto photo);

    Result deletePhoto(Long id);
}
