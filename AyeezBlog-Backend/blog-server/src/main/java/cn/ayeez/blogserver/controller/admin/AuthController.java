package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.dto.response.LoginInfo;
import cn.ayeez.blogserver.service.postServer.AuthServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AuthServer authServer;

    /**
     * 我登录
     */
    @RequestMapping("/login")
    public Result login(@RequestBody String username,String password) {
        log.info("用户登录，账号：{}，密码{}",username,password);
        LoginInfo loginInfo = authServer.login(username,password);
        if(loginInfo!=null){

        return Result.success(loginInfo);
        }else{
            return Result.error(403,"账号密码错误");
        }
    }




}
