package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @DubboReference(group = "upload")
    FileService fileService;

    /**
     * MultipartFile改为可序列化的模式
     * @param file
     * @return
     * @throws JSONException
     */
    @PostMapping("/uploadFile")
    @Operation(summary = "上传文件", description = "上传文件")
    public Result fileUpload(@RequestParam("file") MultipartFile file) throws JSONException {
        System.out.println("controller++++++++++++++++++++++++++++++++++++");
        byte[] arr=null;
        try{
            arr=file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //获取文件名
        String fileName=file.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        return fileService.upload(arr,fileName,suffixName);
    }
    // 下载到了默认的位置
    
    @GetMapping("/downloadFile")
    @Operation(summary = "下载文件", description = "下载文件")
    public Result fileDownload(HttpServletResponse response, @RequestParam("fileName") String fileName) throws JSONException, IOException {
        return fileService.download(response,fileName);
    }
    
    @GetMapping("/deleteFile")
    @Operation(summary = "删除文件", description = "删除文件")
    public Result deleteFile(HttpServletResponse response, @RequestParam("fileName") String fileName) throws JSONException {
        return fileService.delete(response,fileName);
    }
}
