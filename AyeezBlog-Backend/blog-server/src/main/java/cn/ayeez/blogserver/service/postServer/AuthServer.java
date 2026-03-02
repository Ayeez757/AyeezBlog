package cn.ayeez.blogserver.service.postServer;

import cn.ayeez.blogpojo.bo.Auth;
import cn.ayeez.blogpojo.dto.response.LoginInfo;

public interface AuthServer {


    /**
     * 管理员登录
     * @return
     */
    LoginInfo login(Auth auth);
}
