package com.icbc.codeResolver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.codeResolver.entity
 * @Author: zero
 * @CreateTime: 2024-07-24  08:41
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result implements Serializable {
    private Boolean success;
    private String errorMsg;
    private Object data;
    private Integer total;

    public static Result ok(){
        return new Result(true, null, null,null);
    }
    public static Result ok(Object data){
        return new Result(true, null, data,null);
    }
    public static Result ok(List<?> data, Integer total){
        return new Result(true, null, data,total);
    }
    public static Result fail(String errorMsg){
        return new Result(false, errorMsg, null,null);
    }
}
