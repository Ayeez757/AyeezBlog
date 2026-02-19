package cn.ayeez.blogcommon.util;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 统一 API 响应结果封装类
 * 用于所有 Controller 的返回值，保证接口格式一致
 *
 * @param <T> 响应数据的类型
 */
@Data
@Accessors(chain = true) // 支持链式调用：new Result<>().setCode(200).setMessage("OK")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     * 200: 成功
     * 400: 参数错误
     * 401: 未认证
     * 403: 无权限
     * 500: 服务器内部错误
     */
    private int code;

    /**
     * 响应消息（给前端看的提示）
     */
    private String message;

    /**
     * 响应数据（业务数据）
     */
    private T data;

    // ===== 快速构造方法 =====

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<T>().setCode(200).setMessage("操作成功");
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<T>().setCode(200).setMessage("操作成功").setData(data);
    }

    /**
     * 失败响应（自定义错误码和消息）
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<T>().setCode(code).setMessage(message);
    }

    /**
     * 通用失败（500）
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}