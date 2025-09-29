package com.ssssssss.milvus.model;

import lombok.Data;
import java.util.Map;

/**
 * 集合信息实体类，用于封装Milvus集合的基本信息
 *
 * @author 冰点
 */
@Data
public class CollectionInfo {
    private String name;
    private String description;
    private int dimension;
    private long count;
    private String status;
    
    public CollectionInfo(String name, String description, int dimension, long count, String status) {
        this.name = name;
        this.description = description;
        this.dimension = dimension;
        this.count = count;
        this.status = status;
    }
    
    public Map<String, Object> toMap() {
        return Map.of(
            "name", name,
            "description", description,
            "dimension", dimension,
            "count", count,
            "status", status
        );
    }
}