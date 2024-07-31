package com.icbc.codeResolver.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @BelongsProject: project3
 * @BelongsPackage: com.icbc.codeResolver.utils
 * @Author: zero
 * @CreateTime: 2024-07-31  09:24
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class AsyncTaskProgress implements Serializable {
    private static final long serialVersionUID = 1L;
    private String status; // 任务状态：PENDING-进行中，SUCCESS-成功，FAILURE-失败
    private Integer progress; // 任务进度：0-100
    private String result; // 任务结果（仅在状态为SUCCESS时有用）
    private String error; // 任务失败原因（仅在状态为FAILURE时有用）

}
