package cn.ayeez.blogserver.service.postServer;

import cn.ayeez.blogpojo.dto.response.LoginInfo;

public interface AuthServer {


    /**
     * 管理员登录
     * @param username
     * @param password
     * @return
     */
    LoginInfo login(String username, String password);
}
