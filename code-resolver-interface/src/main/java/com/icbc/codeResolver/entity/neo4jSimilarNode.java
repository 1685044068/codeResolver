package com.icbc.codeResolver.entity;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.codeResolver.entity
 * @Author: zero
 * @CreateTime: 2024-07-29  08:58
 * @Description: TODO
 * @Version: 1.0
 */
import lombok.Data;

import java.io.Serializable;
@Data
public class neo4jSimilarNode implements Serializable, Comparable<neo4jSimilarNode>{
    public neo4jNode from;
    public neo4jNode to;
    public Double similarity;

    public neo4jSimilarNode(neo4jNode from, neo4jNode to, Double similarity) {
        this.from = from;
        this.to = to;
        this.similarity = similarity;
    }

    @Override
    public int compareTo(neo4jSimilarNode o) {
        // TODO Auto-generated method stub

        int result1=(int)(o.getSimilarity()*100-this.getSimilarity()*100);
        return result1;

    }
}