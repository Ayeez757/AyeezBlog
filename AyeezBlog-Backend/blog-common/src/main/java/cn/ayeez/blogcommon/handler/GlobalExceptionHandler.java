package cn.ayeez.blogcommon.handler;

import cn.ayeez.blogcommon.exception.AuthenticationException;
import cn.ayeez.blogcommon.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public Result handleAuthenticationException(AuthenticationException e) {
        log.info("认证异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
}
