package com.icbc.codeResolver.service;

import cn.hutool.core.io.FileUtil;

import com.icbc.codeResolver.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;

import java.util.Objects;


@Service
@Slf4j
@DubboService(group = "upload")
public class UploadFileServiceImpl implements UploadFileService {
    @Value("${file.upload.dir}")
    private String uploadFilePath;
    @Override
    public Result upload(MultipartFile file) throws JSONException {
        if (file.isEmpty()) {
            return Result.fail("错误！空文件!");
        }
        // 文件名
        String fileName = file.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        log.info("上传文件名称为:{}, 后缀名为:{}!", fileName, suffixName);

        File fileTempObj = new File(uploadFilePath + "/" + fileName);
        // 检测目录是否存在
        if (!fileTempObj.getParentFile().exists()) {
            fileTempObj.getParentFile().mkdirs();
        }
        // 使用文件名称检测文件是否已经存在
        if (fileTempObj.exists()) {
            return Result.fail("错误！文件已经存在!");
        }

        try {
            FileUtil.writeBytes(file.getBytes(), fileTempObj);
        } catch (Exception e) {
            log.error("发生错误: {}", e);
            return Result.fail("错误！"+e.getMessage());
        }
        return Result.ok(uploadFilePath + "/" + fileName);
    }

    @Override
    public Result upload(byte[] file,String fileName,String suffixName) throws JSONException {
        System.out.println("++++++++++++++++++++++++++++++++++进入一次");
        if (Objects.isNull(file)) {
            return Result.fail("错误！空文件!");
        }
        log.info("上传文件名称为:{}, 后缀名为:{}!", fileName, suffixName);
        File fileTempObj = new File(uploadFilePath + "/" + fileName);
        // 检测目录是否存在
        if (!fileTempObj.getParentFile().exists()) {
            fileTempObj.getParentFile().mkdirs();
        }
        // 使用文件名称检测文件是否已经存在
        if (fileTempObj.exists()) {
            System.out.println("#########################################文件已存在");
            return Result.fail("错误！文件已经存在!");
        }
        try {
            FileUtil.writeBytes(file, fileTempObj);
        } catch (Exception e) {
            log.error("发生错误: {}", e);
            return Result.fail("错误！"+e.getMessage());
        }
        return Result.ok(uploadFilePath + "/" + fileName);
    }

}
