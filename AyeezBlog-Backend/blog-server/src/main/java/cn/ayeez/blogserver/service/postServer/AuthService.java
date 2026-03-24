package cn.ayeez.blogserver.service.postServer;

import cn.ayeez.blogpojo.bo.Auth;
import cn.ayeez.blogpojo.dto.response.LoginInfo;

public interface AuthService {


    /**
     * 管理员登录
     *
     * @param auth 登录参数
     * @return 登录成功后返回登录信息，失败返回 null
     */
    LoginInfo login(Auth auth);
}
