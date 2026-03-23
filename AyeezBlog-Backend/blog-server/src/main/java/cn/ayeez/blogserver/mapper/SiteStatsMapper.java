package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.dto.response.SiteVisitStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SiteStatsMapper {

    void initStatsRow();

    int increasePageViews();

    int insertVisitor(@Param("visitorKey") String visitorKey,
                      @Param("ipAddress") String ipAddress,
                      @Param("userAgent") String userAgent,
                      @Param("visitPath") String visitPath);

    int touchVisitor(@Param("visitorKey") String visitorKey);

    SiteVisitStats getStats();
}
