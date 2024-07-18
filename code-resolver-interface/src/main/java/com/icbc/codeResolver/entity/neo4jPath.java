package com.icbc.codeResolver.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.codeResolver.entity
 * @Author: zero
 * @CreateTime: 2024-07-18  09:37
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class neo4jPath implements Serializable {
    public List<neo4jNode> pathMember;
    public Integer pathLen;

    public neo4jPath(){
        pathMember=new ArrayList<>();
        pathLen=0;
    }


}
