package com.icbc.codeResolver.controller;

import com.icbc.codeResolver.config.CommonConfig;
import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.service.FileService;
import com.icbc.codeResolver.utils.FileUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.stereotype.Controller;
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
        return fileService.multiUpload(FileUtils.MultiFilesToFileInfo(files));
    }
    // 下载到了默认的位置
    @GetMapping("/downloadFile")
    public Result fileDownload(HttpServletResponse response, @RequestParam("fileName") String fileName) throws JSONException, IOException {
        return fileService.download(response,fileName);
    }

    @GetMapping("/deleteFile")
    public Result deleteFile(HttpServletResponse response, @RequestParam("fileName") String fileName) throws JSONException {
        return fileService.delete(response,fileName);
    }

    // /**
    //  * 多个文件上传
    //  *
    //  * @param files
    //  * @return
    //  * @throws JSONException
    //  */
//    @ResponseBody
//    @PostMapping("/uploadFiles")
//    public String fileUploads(@RequestParam("files") MultipartFile files[]) throws JSONException {
//        JSONObject result = new JSONObject();
//
//        for (int i = 0; i < files.length; i++) {
//            String fileName = files[i].getOriginalFilename();
//            File dest = new File(uploadFilePath + '/' + fileName);
//            if (!dest.getParentFile().exists()) {
//                dest.getParentFile().mkdirs();
//            }
//            try {
//                files[i].transferTo(dest);
//            } catch (Exception e) {
//                log.error("发生错误: {}", e);
//                result.put("error", e.getMessage());
//                return result.toString();
//            }
//        }
//        result.put("success", "文件上传成功!");
//
//        return result.toString();
//    }
//
//    /**
//     * 多个文件上传
//     *
//     * @param files
//     * @return
//     * @throws JSONException
//     */
//    @ResponseBody
//    @PostMapping("/uploadFiles02")
//    public String fileUploads(String name, @RequestParam("files") MultipartFile files[]) throws JSONException {
//        System.out.println(name);
//        JSONObject result = new JSONObject();
//
//        for (int i = 0; i < files.length; i++) {
//            String fileName = files[i].getOriginalFilename();
//            File dest = new File(uploadFilePath + '/' + fileName);
//            if (!dest.getParentFile().exists()) {
//                dest.getParentFile().mkdirs();
//            }
//            try {
//                files[i].transferTo(dest);
//            } catch (Exception e) {
//                log.error("发生错误: {}", e);
//                result.put("code", 400);
//                result.put("error", e.getMessage());
//                return result.toString();
//            }
//        }
//        result.put("code", 200);
//        result.put("success", "文件上传成功!");
////        csvService.transmit();
//        return result.toString();
//    }




}
