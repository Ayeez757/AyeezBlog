package cn.ayeez.blogserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;


@ServletComponentScan({"cn.ayeez.blogserver", "cn.ayeez.blogcommon"})
@SpringBootApplication(scanBasePackages = {
        "cn.ayeez.blogserver",
        "cn.ayeez.blogcommon"
})
@MapperScan("cn.ayeez.blogserver.mapper") // 扫描Mapper接口
public class BlogServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogServerApplication.class, args);
    }

}
