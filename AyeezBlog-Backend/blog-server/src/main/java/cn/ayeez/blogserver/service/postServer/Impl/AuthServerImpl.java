package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogpojo.dto.response.LoginInfo;
import cn.ayeez.blogserver.service.postServer.AuthServer;
import org.springframework.stereotype.Service;

@Service
public class AuthServerImpl implements AuthServer {

    //TODO 登录要重写或者其他方法，因为后端是部署上线的，接口开放不行
    @Override
    public LoginInfo login(String username, String password) {
        if(username.equals("Ayeez")&&password.equals("123456")){
            return new LoginInfo(1,"Ayeez","阿叶Ayeez",null);
        }else{
            return null;
        }

    }
}
