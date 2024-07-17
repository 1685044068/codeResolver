package com.upload.controller;

import cn.hutool.core.io.FileUtil;
//import com.common.CSVService;
import com.upload.service.UploadFileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**

 */
@Controller
@RequestMapping("file")
@Slf4j
public class FileController {
    @Value("${file.upload.dir}")
    private String uploadFilePath;

    @GetMapping(value = {"", "/", "/index"})
    public String index() {
        return "upload";
    }
//    @DubboReference
//    private CSVService csvService;
    @Autowired
    private UploadFileService uploadFileService;

    @ResponseBody
    @PostMapping("/uploadFile")
    public String fileUpload(@RequestParam("file") MultipartFile file) throws JSONException {
        MultipartFile transFile=file;
        return uploadFileService.upload(transFile);
    }

    /**
     * 多个文件上传
     *
     * @param files
     * @return
     * @throws JSONException
     */
    @ResponseBody
    @PostMapping("/uploadFiles")
    public String fileUploads(@RequestParam("files") MultipartFile files[]) throws JSONException {
        JSONObject result = new JSONObject();

        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getOriginalFilename();
            File dest = new File(uploadFilePath + '/' + fileName);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            try {
                files[i].transferTo(dest);
            } catch (Exception e) {
                log.error("发生错误: {}", e);
                result.put("error", e.getMessage());
                return result.toString();
            }
        }
        result.put("success", "文件上传成功!");

        return result.toString();
    }

    /**
     * 多个文件上传
     *
     * @param files
     * @return
     * @throws JSONException
     */
    @ResponseBody
    @PostMapping("/uploadFiles02")
    public String fileUploads(String name, @RequestParam("files") MultipartFile files[]) throws JSONException {
        System.out.println(name);
        JSONObject result = new JSONObject();

        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getOriginalFilename();
            File dest = new File(uploadFilePath + '/' + fileName);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            try {
                files[i].transferTo(dest);
            } catch (Exception e) {
                log.error("发生错误: {}", e);
                result.put("code", 400);
                result.put("error", e.getMessage());
                return result.toString();
            }
        }
        result.put("code", 200);
        result.put("success", "文件上传成功!");
//        csvService.transmit();
        return result.toString();
    }


    // 下载到了默认的位置
    @ResponseBody
    @GetMapping("/downloadFile")
    public String fileDownload(HttpServletResponse response, @RequestParam("fileName") String fileName) throws JSONException, IOException {
        JSONObject result = new JSONObject();

        File file = new File(uploadFilePath + '/' + fileName);
        if (!file.exists()) {
            result.put("error", "下载文件不存在!");
            return result.toString();
        }

        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        // 原生的方式
        // try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));) {
        //     byte[] buff = new byte[1024];
        //     OutputStream os = response.getOutputStream();
        //     int i = 0;
        //     while ((i = bis.read(buff)) != -1) {
        //         os.write(buff, 0, i);
        //         os.flush();
        //     }
        // } catch (IOException e) {
        //     log.error("发生错误: {}", e);
        //     result.put("error", e.getMessage());
        //     return result.toString();
        // }
        // 简单方式: 方式1
        // byte[] bytes = FileCopyUtils.copyToByteArray(file);
        // 简单方式: 方式2
        byte[] readBytes = FileUtil.readBytes(file);
        OutputStream os = response.getOutputStream();
        os.write(readBytes);
        result.put("success", "下载成功!");
        return result.toString();
    }


    @ResponseBody
    @PostMapping("/deleteFile")
    public String deleteFile(HttpServletResponse response, @RequestParam("fileName") String fileName) throws JSONException {
        JSONObject result = new JSONObject();

        File file = new File(uploadFilePath + '/' + fileName);
        // 判断文件不为null或文件目录存在
        if (file == null || !file.exists()) {
            result.put("success", "文件不存在!");
            return result.toString();
        }
        try {
            if (file.isFile()) file.delete();
            else {
                // 文件夹, 需要先删除文件夹下面所有的文件, 然后删除
                for (File temp : file.listFiles()) {
                    temp.delete();
                }
                file.delete();
            }
        } catch (Exception e) {
            log.error("发生错误: {}", e);
            result.put("error", e.getMessage());
            return result.toString();
        }
        result.put("success", "删除成功!");
        return result.toString();
    }

}
