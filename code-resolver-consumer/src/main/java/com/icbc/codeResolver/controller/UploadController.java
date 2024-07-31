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
    private static Logger logger = Logger.getLogger(UploadController.class);

    @DubboReference(group = "upload")
    FileService fileService;

    /**
     * MultipartFile改为可序列化的模式
     * @param files
     * @return
     * @throws JSONException
     */
    @PostMapping("/uploadFile")
    @Operation(summary = "上传文件", description = "上传文件")
    public Result fileUpload(@RequestParam("files") MultipartFile[] files) throws JSONException, IOException {
        logger.info("+++++++++++++++++开始上传文件+++++++++++++++++");
        List<FileInfo> fileInfos = FileUtils.MultiFilesToFileInfo(files);
        return fileService.multiUpload(fileInfos);
    }
    // 下载到了默认的位置
    
    @GetMapping("/downloadFile")
    @Operation(summary = "下载文件", description = "下载文件")
    public Result fileDownload(HttpServletResponse response, @RequestParam("fileName") String fileName) throws JSONException, IOException {
        logger.info("开始下载文件");
        return fileService.download(response,fileName);
    }
    
    @GetMapping("/deleteFile")
    @Operation(summary = "删除文件", description = "删除文件")
    public Result deleteFile(HttpServletResponse response, @RequestParam("fileName") String fileName) throws JSONException {
        logger.info("开始删除文件");
        return fileService.delete(response,fileName);
    }


}
