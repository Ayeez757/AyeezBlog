package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.dto.response.SiteTrafficHistoryPoint;
import cn.ayeez.blogpojo.dto.response.SiteVisitStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

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

    /**
     * 累积当日 PV。
     */
    void increasePageViewsDaily(@Param("statDate") LocalDate statDate);

    /**
     * 尝试插入（visitorKey, 日期）UV 明细；成功返回 1（该 visitor 当天首次出现）。
     */
    int insertVisitorDaily(@Param("visitorKey") String visitorKey, @Param("statDate") LocalDate statDate);

    /**
     * 累积当日 UV 聚合值（只在 insertVisitorDaily 返回 >0 时调用）。
     */
    void increaseUniqueVisitorsDaily(@Param("statDate") LocalDate statDate);

    /**
     * 查询最近 N 天（含当天）的历史曲线数据（不保证每天都有记录）。
     */
    List<SiteTrafficHistoryPoint> getDailyHistory(@Param("days") int days);
}
