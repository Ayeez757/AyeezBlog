package cn.ayeez.blogserver.mapper;

import cn.ayeez.blogpojo.po.Auth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 管理员登录Mapper接口
 */
@Mapper
public interface AuthMapper {

    Auth findByUsername(@Param("username") String username);

    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
