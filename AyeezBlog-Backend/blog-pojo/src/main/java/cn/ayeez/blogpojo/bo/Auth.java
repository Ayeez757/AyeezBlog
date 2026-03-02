package cn.ayeez.blogpojo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用于封装登录信息从登录controller传递给server以及在server之间传递
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Auth {
    private Integer id;
    private String username; //用户名
    private String password; //密码

}
