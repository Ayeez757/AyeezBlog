package cn.ayeez.blogpojo.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用于封装登录信息,从数据库mapper层传到server层
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Auth {
    private Integer id;
    private String username; //用户名
    private String password; //密码
    private String nickname;

}