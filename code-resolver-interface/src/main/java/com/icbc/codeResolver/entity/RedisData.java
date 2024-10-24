package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.codeResolver.entity
 * @Author: zero
 * @CreateTime: 2024-07-18  11:09
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class RedisData implements Serializable {
    private LocalDateTime expireTime;
    private Object data;
}
