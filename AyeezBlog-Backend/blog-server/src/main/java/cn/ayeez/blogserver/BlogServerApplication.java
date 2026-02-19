package cn.ayeez.blogserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "cn.ayeez.blogserver", // 启动类所在包
        "cn.ayeez.blogcommon" //
})
@MapperScan("cn.ayeez.blogserver.mapper") // 扫描Mapper接口
public class BlogServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogServerApplication.class, args);
    }

}
