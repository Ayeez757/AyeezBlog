package cn.ayeez.blogpojo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteVisitStats {
    private Long pageViews;
    private Long uniqueVisitors;
}
