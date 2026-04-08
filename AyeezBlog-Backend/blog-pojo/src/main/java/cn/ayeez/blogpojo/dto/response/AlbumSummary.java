package cn.ayeez.blogpojo.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AlbumSummary {
    private Long id;
    private String title;
    private String description;
    private Integer sort;
    private Integer photoCount;
    private List<String> coverImages = new ArrayList<>();
}
