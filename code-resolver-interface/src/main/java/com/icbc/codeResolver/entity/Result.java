package com.icbc.codeResolver.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.codeResolver.entity
 * @Author: zero
 * @CreateTime: 2024-07-24  08:41
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String msg;
    private T data;
    private Map map=new HashMap();
    public static<T> Result<T> successful(T object){
        Result<T> result=new Result<>();
        result.setCode(1);
        result.setData(object);
        return result;
    }
    public static<T> Result<T> error(String msg){
        Result<T> result=new Result<>();
        result.setCode(0);
        result.setMsg(msg);
        return result;
    }
    public Result<T> add(String key,Object value){
        this.map.put(key,value);
        return this;
    }
}

