package com.ssssssss.milvus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 视图控制器类，用于处理前端页面请求
 *
 * @author 冰点
 */
@Controller
public class ViewController {

    /**
     * 提供Milvus Web UI主页
     */
    @GetMapping("/")
    public String index() {
        return "index.html";
    }
}
