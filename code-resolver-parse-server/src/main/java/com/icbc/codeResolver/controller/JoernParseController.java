package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.service.JoernParseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/parse")
@RestController
@Slf4j
public class JoernParseController {
    @Autowired
    JoernParseService joernParseService;
    @GetMapping("/parseCode")
    public String parseAndImport(@RequestParam("url") String url){
        return joernParseService.parse(url);
    }
}
