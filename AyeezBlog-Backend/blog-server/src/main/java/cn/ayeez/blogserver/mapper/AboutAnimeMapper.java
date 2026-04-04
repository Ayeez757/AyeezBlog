package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.entity.AboutAnime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AboutAnimeMapper {

    List<AboutAnime> listOrdered();

    Integer selectMaxSort();

    void insert(AboutAnime row);

    void update(AboutAnime row);

    /** 仅更新展示字段，不改 sort（排序只走 reorder 接口） */
    void updateContent(AboutAnime row);

    void deleteById(@Param("id") Long id);

    /**
     * 按 ids 顺序一次性写回 sort（0..n-1），单条 SQL，避免逐行 update
     */
    void reorderSorts(@Param("ids") List<Long> ids);

    int countByIds(@Param("ids") List<Long> ids);

    int countAll();
}
