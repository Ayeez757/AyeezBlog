package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关于账户信息的
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * 我登录
     */
    //TODO 登录接口待完成
    @RequestMapping("/login")
    public Result login(@RequestBody String username,String password) {
        log.info("用户登录，账号：{}，密码{}",username,password);

        return Result.success();
    }




}
