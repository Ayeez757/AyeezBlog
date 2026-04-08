package cn.ayeez.blogserver.controller.user;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.response.AlbumDetail;
import cn.ayeez.blogserver.service.postServer.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/album")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @GetMapping("/list")
    public Result list() {
        return Result.success(albumService.listPublicAlbums());
    }

    @GetMapping("/get")
    public Result get(@RequestParam Long id) {
        AlbumDetail detail = albumService.getPublicAlbumDetail(id);
        if (detail == null) {
            return Result.error(404, "相册不存在");
        }
        return Result.success(detail);
    }
}
