package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.service.FileService;
import com.icbc.codeResolver.utils.FileUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;


@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;


    @GetMapping(value = {"", "/", "/index"})
    public String index() {
        return "index";
    }

    @PostMapping("/uploadFile")
    public Result fileUpload(@RequestParam("files") MultipartFile[] files) throws IOException {
        fileService.multiUpload(FileUtils.multipartFilesToFileInfo(files));
        return Result.success("");
    }
    // 下载到了默认的位置
    @GetMapping("/downloadFile")
    public Result fileDownload(HttpServletResponse response, @RequestParam("fileName") String fileName) throws JSONException, IOException {
        fileService.download(response,fileName);
        return Result.success("");
    }

    @GetMapping("/deleteFile")
    public Result deleteFile(@RequestParam("fileName") String fileName) throws JSONException {
        fileService.delete(fileName);
        return Result.success("");
    }

}
