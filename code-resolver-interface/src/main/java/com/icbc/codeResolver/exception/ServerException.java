package com.icbc.codeResolver.exception;

import com.icbc.codeResolver.entity.ResultCode;

/**
 * 服务端异常
 * @author zmq
 */
public class ServerException extends GlobalException {

    public ServerException(String message) {
        super(ResultCode.SERVER_ERROR,message);
    }
}
