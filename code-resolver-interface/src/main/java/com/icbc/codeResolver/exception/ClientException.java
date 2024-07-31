package com.icbc.codeResolver.exception;

import com.icbc.codeResolver.entity.ResultCode;

/**
 * 客户端异常
 * @author zmq
 */
public class ClientException extends GlobalException {

    public ClientException(String message) {
        super(ResultCode.CLIENT_FAILED,message);
    }
}
