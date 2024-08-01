package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.codeResolver.entity
 * @Author: zmq
 * @CreateTime: 2024-07-31  22:06
 * @Description: 返回前端的结果集
 * @Version: 1.0
 */
@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;

    public Result(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    public Result(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 请求正常完成，返回请求结果
     * @param data 返回前端的数据
     * @return {@link Result}<{@link T}>
     */
    public static<T> Result<T> success(T data){
        return new Result<>(ResultCode.SUCCESS.getCode(),data);
    }

    /**
     * 请求失败，根据错误码返回
     * @param errorCode 枚举对象，返回的错误码：400 or 500
     * @param msg 返回的错误信息
     * @return {@link Result}<{@link T}>
     */
    public static<T> Result<T> error(ResultCode errorCode, String msg){
        return errorCode==ResultCode.CLIENT_ERROR ? clientError(msg): serverError(msg);
    }

    /**
     * 由客户端引起的请求失败，如参数填写错误，请求的文件不存在等
     * @param msg 返回的错误信息
     * @return {@link Result}<{@link T}>
     */
    public static<T> Result<T> clientError(String msg){
        return new Result<>(ResultCode.CLIENT_ERROR.getCode(), msg);
    }
    /**
     * 由客户端引起的请求失败，如参数填写错误，请求的文件不存在等
     * <br/>
     * 可自定义状态码，用于接收springmvc抛出的异常
     * @param msg 返回的错误信息
     * @return {@link Result}<{@link T}>
     */
    public static<T> Result<T> clientErrorWithCode(int code,String msg){
        return new Result<>(code, msg);
    }

    /**
     * 由服务端引起的请求失败，如IO操作失败，空指针异常等
     * @param msg 返回的错误信息
     * @return {@link Result}<{@link T}>
     */
    public static<T> Result<T> serverError(String msg){
        return new Result<>(ResultCode.SERVER_ERROR.getCode(), msg);
    }

    /**
     * 由服务端引起的请求失败，如IO操作失败，空指针异常等
     * 可自定义状态码，用于接收springmvc抛出的异常
     * @param msg 返回的错误信息
     * @return {@link Result}<{@link T}>
     */
    public static<T> Result<T> serverErrorWithCode(int code, String msg){
        return new Result<>(code, msg);
    }

    /**
     * 请求正在处理中，如代码正在解析中，并返回执行进度信息
     * @param msg 返回的执行进度信息
     * @param taskID 返回的任务id
     * @return {@link Result}<{@link T}>
     */
    public static <T> Result<T> processing(String msg, T taskID){
        return new Result<>(ResultCode.PROCESSING.getCode(), taskID);
    }

}

