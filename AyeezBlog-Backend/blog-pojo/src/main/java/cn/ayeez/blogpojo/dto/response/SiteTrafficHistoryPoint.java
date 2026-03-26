package cn.ayeez.blogpojo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 站点流量历史曲线数据点（按天）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteTrafficHistoryPoint {
    /**
     * yyyy-MM-dd
     */
    private String date;

    /**
     * 当天 PV
     */
    private Long pageViews;

    /**
     * 当天 UV
     */
    private Long uniqueVisitors;
}

