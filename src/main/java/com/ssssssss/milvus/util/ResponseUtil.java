package com.ssssssss.milvus.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.Map;

/**
 * 响应工具类，用于统一处理API接口的响应结果
 *
 * @author 冰点
 */
public class ResponseUtil {
    
    /**
     * 成功响应
     */
    public static ResponseEntity<?> success(String message) {
        return ResponseEntity.ok(Collections.singletonMap("message", message));
    }
    
    /**
     * 成功响应带数据
     */
    public static ResponseEntity<?> success(String message, Object data) {
        return ResponseEntity.ok(Map.of(
            "message", message,
            "data", data
        ));
    }
    
    /**
     * 错误响应
     */
    public static ResponseEntity<?> error(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("error", message));
    }
    
    /**
     * 错误响应带状态码
     */
    public static ResponseEntity<?> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Collections.singletonMap("error", message));
    }
    
    /**
     * 服务器内部错误
     */
    public static ResponseEntity<?> serverError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", message));
    }
}