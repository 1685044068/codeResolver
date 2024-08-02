package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.service.FileService;
import com.icbc.codeResolver.utils.FileInfo;
import com.icbc.codeResolver.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.log4j.Logger;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.coderesolverconsumber.controller
 * @Author: zero
 * @CreateTime: 2024-07-22  09:20
 * @Description: 分布式调用controller
 * @Version: 1.0
 */
@RestController
@RequestMapping(value = "/file")
@Tag(name = "DubboUpload", description = "upload接口")
public class UploadController {
    private static final Logger logger = Logger.getLogger(UploadController.class);

    @DubboReference(group = "upload")
    FileService fileService;

    @GetMapping(value = {"", "/", "/index"})
    public String index() {
        return "index";
    }

    @PostMapping("/uploadFile")
    public Result fileUpload(@RequestParam("files") MultipartFile[] files) {
        fileService.multiUpload(FileUtils.multipartFilesToFileInfo(files));
        return Result.success("");
    }
    // 下载到了默认的位置
    @GetMapping("/downloadFile")
    public Result fileDownload(HttpServletResponse response, @RequestParam("fileName") String fileName) throws IOException {
        fileService.download(response,fileName);
        return Result.success("");
    }

    @GetMapping("/deleteFile")
    public Result deleteFile(@RequestParam("fileName") String fileName)  {
        fileService.delete(fileName);
        return Result.success("");
    }


}
