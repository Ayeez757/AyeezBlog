package cn.ayeez.blogpojo.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AboutAnimeReorderBody {

    private List<Long> ids;
}
