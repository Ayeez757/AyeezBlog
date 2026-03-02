package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.bo.Auth;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员登录Mapper接口
 */
@Mapper
public interface AuthMapper {

    cn.ayeez.blogpojo.po.Auth loginByUsernameAndPassword(Auth auth);
}
