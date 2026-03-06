package cn.ayeez.blogserver.controller.admin;

import cn.ayeez.blogcommon.util.Result;
import cn.ayeez.blogpojo.bo.Auth;
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
@RequestMapping("/admin")
public class AdminAuthController {

    @Autowired
    private AuthServer authServer;

    /**
     * 我登录
     */
    //TODO 登录接口已经重写，能正常登录返回token，但是需要加一个加密功能，现在数据库里存的还是未加密临时密码
    @RequestMapping("/login")
    public Result login(@RequestBody Auth auth) {
        String username = auth.getUsername();
        String password = auth.getPassword();
        log.info("用户登录，账号：{}，密码{}",username,password);
        LoginInfo loginInfo = authServer.login(auth);
        if(loginInfo!=null){
        return Result.success(loginInfo);
        }else{
            return Result.error(403,"账号密码错误");
        }
    }




}
