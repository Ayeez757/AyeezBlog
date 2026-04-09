package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.entity.Album;
import cn.ayeez.blogpojo.entity.AlbumPhoto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlbumMapper {
    List<Album> listAlbums();

    Album getAlbumById(@Param("id") Long id);

    List<AlbumPhoto> listPhotosByAlbumIds(@Param("albumIds") List<Long> albumIds, @Param("limitPerAlbum") Integer limitPerAlbum);

    List<AlbumPhoto> listPhotosByAlbumId(@Param("albumId") Long albumId);

    Integer countPhotosByAlbumId(@Param("albumId") Long albumId);

    void insertAlbum(Album album);

    void updateAlbum(Album album);

    void deleteAlbumById(@Param("id") Long id);

    void clearDefaultCoverSource();

    int markAlbumAsDefaultCoverSource(@Param("id") Long id);

    void insertPhoto(AlbumPhoto photo);

    void updatePhoto(AlbumPhoto photo);

    void deletePhotoById(@Param("id") Long id);

    Integer selectMaxAlbumSort();

    Integer selectMaxPhotoSort(@Param("albumId") Long albumId);
}
