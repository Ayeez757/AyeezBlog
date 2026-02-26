package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogpojo.dto.response.LoginInfo;
import cn.ayeez.blogserver.service.postServer.AuthServer;
import org.springframework.stereotype.Service;

@Service
public class AuthServerImpl implements AuthServer {

    //TODO 登录写死的，待完成（不急，反正不打算部署服务端上线）
    @Override
    public LoginInfo login(String username, String password) {
        if(username.equals("Ayeez")&&password.equals("123456")){
            return new LoginInfo(1,"Ayeez","阿叶Ayeez",null);
        }else{
            return null;
        }
    }
}
