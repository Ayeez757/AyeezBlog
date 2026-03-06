package cn.ayeez.blogserver.service.postServer.Impl;

import cn.ayeez.blogpojo.bo.Auth;
import cn.ayeez.blogpojo.dto.response.LoginInfo;
import cn.ayeez.blogserver.mapper.AuthMapper;
import cn.ayeez.blogserver.service.postServer.AuthServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.ayeez.blogcommon.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AuthServerImpl implements AuthServer {
    @Autowired
    private AuthMapper authMapper;


    @Override
    public LoginInfo login(Auth auth) {
        cn.ayeez.blogpojo.po.Auth a = authMapper.loginByUsernameAndPassword(auth);
        if (a != null) {
            log.info("登录成功，信息：{}", a);
            Map<String, Object> claims = new HashMap<>();
            claims.put("username", a.getUsername());
            String jwt = JwtUtil.generateToken(claims);
            LoginInfo loginInfo = new LoginInfo(a.getId(), a.getUsername(), a.getNickname(), jwt);
            return loginInfo;
        }
        return null;

    }
}
