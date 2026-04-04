package cn.ayeez.blogserver.controller.user;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.entity.AboutAnime;
import cn.ayeez.blogserver.service.postServer.AboutAnimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 关于页公开数据
 */
@Slf4j
@RestController
@RequestMapping("/about")
public class AboutController {

    @Autowired
    private AboutAnimeService aboutAnimeService;

    @GetMapping("/anime/list")
    public Result listAnime() {
        List<AboutAnime> list = aboutAnimeService.listPublic();
        return Result.success(list);
    }
}
