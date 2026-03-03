package cn.ayeez.blogcommon.exception;

public class AuthenticationException extends RuntimeException{

    private int code;

    public AuthenticationException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
