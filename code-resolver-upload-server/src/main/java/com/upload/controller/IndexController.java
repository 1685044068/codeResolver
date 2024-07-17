package com.upload.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: z.g.y
 * @date: 2021/1/28
 */
@RestController
public class IndexController {
    @GetMapping(value = {"", "/", "/index"})
    public String index() {
        return "index";
    }
}
