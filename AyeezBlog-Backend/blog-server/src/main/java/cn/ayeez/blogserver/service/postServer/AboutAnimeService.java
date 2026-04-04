package cn.ayeez.blogserver.service.postServer;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.entity.AboutAnime;

import java.util.List;

public interface AboutAnimeService {

    List<AboutAnime> listPublic();

    List<AboutAnime> listAll();

    Result add(AboutAnime row);

    Result update(AboutAnime row);

    Result delete(Long id);

    Result reorder(List<Long> ids);
}
