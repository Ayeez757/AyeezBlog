package cn.ayeez.blogpojo.dto.response;

import cn.ayeez.blogpojo.entity.AlbumPhoto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AlbumDetail {
    private Long id;
    private String title;
    private String description;
    private Integer sort;
    private List<String> coverImages = new ArrayList<>();
    private List<AlbumPhoto> photos = new ArrayList<>();
}
