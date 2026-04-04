package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.request.AboutAnimeReorderBody;
import cn.ayeez.blogpojo.entity.AboutAnime;
import cn.ayeez.blogserver.service.postServer.AboutAnimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端：关于页追番列表
 */
@Slf4j
@RestController
@RequestMapping("/admin/about/anime")
public class AdminAboutAnimeController {

    @Autowired
    private AboutAnimeService aboutAnimeService;

    @GetMapping("/list")
    public Result list() {
        return Result.success(aboutAnimeService.listAll());
    }

    @PostMapping("/add")
    public Result add(@RequestBody AboutAnime row) {
        return aboutAnimeService.add(row);
    }

    @PutMapping("/update")
    public Result update(@RequestBody AboutAnime row) {
        return aboutAnimeService.update(row);
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam Long id) {
        return aboutAnimeService.delete(id);
    }

    @PutMapping("/reorder")
    public Result reorder(@RequestBody AboutAnimeReorderBody body) {
        if (body == null) {
            return Result.error(400, "请求体不能为空");
        }
        return aboutAnimeService.reorder(body.getIds());
    }
}
