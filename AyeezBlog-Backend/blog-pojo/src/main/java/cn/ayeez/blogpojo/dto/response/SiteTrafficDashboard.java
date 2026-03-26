package cn.ayeez.blogpojo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理端控制台：流量数据面板返回值。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteTrafficDashboard {
    private Long pageViews;
    private Long uniqueVisitors;
    private List<SiteTrafficHistoryPoint> history;
}

