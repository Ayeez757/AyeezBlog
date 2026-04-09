package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.entity.Album;
import cn.ayeez.blogpojo.entity.AlbumPhoto;
import cn.ayeez.blogserver.service.postServer.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/album")
public class AdminAlbumController {

    @Autowired
    private AlbumService albumService;

    @GetMapping("/list")
    public Result listAlbums() {
        return Result.success(albumService.listAdminAlbums());
    }

    @PostMapping("/add")
    public Result addAlbum(@RequestBody Album album) {
        return albumService.addAlbum(album);
    }

    @PutMapping("/update")
    public Result updateAlbum(@RequestBody Album album) {
        return albumService.updateAlbum(album);
    }

    @DeleteMapping("/delete")
    public Result deleteAlbum(@RequestParam Long id) {
        return albumService.deleteAlbum(id);
    }

    @PostMapping("/default-cover/set")
    public Result setDefaultCoverAlbum(@RequestParam Long id) {
        return albumService.setDefaultCoverAlbum(id);
    }

    @GetMapping("/photo/list")
    public Result listPhotos(@RequestParam Long albumId) {
        return Result.success(albumService.listAdminPhotos(albumId));
    }

    @PostMapping("/photo/add")
    public Result addPhoto(@RequestBody AlbumPhoto photo) {
        return albumService.addPhoto(photo);
    }

    @PutMapping("/photo/update")
    public Result updatePhoto(@RequestBody AlbumPhoto photo) {
        return albumService.updatePhoto(photo);
    }

    @DeleteMapping("/photo/delete")
    public Result deletePhoto(@RequestParam Long id) {
        return albumService.deletePhoto(id);
    }
}
