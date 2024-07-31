package com.icbc.codeResolver.entity;

/**
 * 返回结果的状态码集合
 * @author zmq
 */
public enum ResultCode {
    /**
     * 100：请求正在处理中
     */
    PROCESSING(100),
    /**
     * 200：请求正常完成
     */
    SUCCESS(200),
    /**
     * 300：请求被重定向
     */
    REDIRECT(300),
    /**
     * 400：客户端错误
     */
    CLIENT_FAILED(400),
    /**
     * 500：服务端错误
     */
    SERVER_FAILED(500);


    private final int code;

    ResultCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
