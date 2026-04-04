package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.entity.AboutAnime;
import cn.ayeez.blogserver.mapper.AboutAnimeMapper;
import cn.ayeez.blogserver.service.postServer.AboutAnimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class AboutAnimeServiceImpl implements AboutAnimeService {

    @Autowired
    private AboutAnimeMapper aboutAnimeMapper;

    @Override
    public List<AboutAnime> listPublic() {
        List<AboutAnime> list = aboutAnimeMapper.listOrdered();
        return list != null ? list : new ArrayList<>();
    }

    @Override
    public List<AboutAnime> listAll() {
        return listPublic();
    }

    @Override
    @Transactional
    public Result add(AboutAnime row) {
        Result err = validate(row, false);
        if (err != null) {
            return err;
        }
        Integer max = aboutAnimeMapper.selectMaxSort();
        row.setSort((max == null || max < 0) ? 0 : max + 1);
        aboutAnimeMapper.insert(row);
        return Result.success();
    }

    @Override
    @Transactional
    public Result update(AboutAnime row) {
        Result err = validate(row, true);
        if (err != null) {
            return err;
        }
        // 管理端编辑只改 URL/标题/链接；sort 仅由「保存排序」reorder 写入，避免本地调序后误点保存即写库
        aboutAnimeMapper.updateContent(row);
        return Result.success();
    }

    @Override
    @Transactional
    public Result delete(Long id) {
        if (id == null) {
            return Result.error(400, "ID 不能为空");
        }
        aboutAnimeMapper.deleteById(id);
        return Result.success();
    }

    @Override
    @Transactional
    public Result reorder(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "排序 ID 列表不能为空");
        }
        int total = aboutAnimeMapper.countAll();
        if (ids.size() != total) {
            return Result.error(400, "排序 ID 数量须与当前记录数一致");
        }
        Set<Long> seen = new HashSet<>();
        for (Long id : ids) {
            if (id == null) {
                return Result.error(400, "排序 ID 不能含空值");
            }
            if (!seen.add(id)) {
                return Result.error(400, "排序 ID 不能重复");
            }
        }
        if (aboutAnimeMapper.countByIds(ids) != ids.size()) {
            return Result.error(400, "排序 ID 与数据库记录不一致");
        }
        aboutAnimeMapper.reorderSorts(ids);
        return Result.success();
    }

    private Result validate(AboutAnime row, boolean requireId) {
        if (row == null) {
            return Result.error(400, "参数不能为空");
        }
        if (requireId && row.getId() == null) {
            return Result.error(400, "ID 不能为空");
        }
        if (row.getImageUrl() == null || row.getImageUrl().trim().isEmpty()) {
            return Result.error(400, "图片地址不能为空");
        }
        row.setImageUrl(row.getImageUrl().trim());
        if (row.getTitle() != null) {
            row.setTitle(row.getTitle().trim());
            if (row.getTitle().isEmpty()) {
                row.setTitle(null);
            }
        }
        if (row.getLinkUrl() != null) {
            row.setLinkUrl(row.getLinkUrl().trim());
            if (row.getLinkUrl().isEmpty()) {
                row.setLinkUrl(null);
            }
        }
        return null;
    }
}
