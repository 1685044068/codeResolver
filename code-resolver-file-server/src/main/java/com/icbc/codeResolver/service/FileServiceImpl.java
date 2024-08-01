package com.icbc.codeResolver.service;

import cn.hutool.core.io.FileUtil;

import com.icbc.codeResolver.config.CommonConfig;
import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.exception.ClientException;
import com.icbc.codeResolver.exception.ServerException;
import com.icbc.codeResolver.utils.FileInfo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.stereotype.Service;


import java.io.File;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;


@Service
@Slf4j
@DubboService(group = "upload", timeout = 10000)
public class FileServiceImpl implements FileService {

    String uploadFilePath;

    public FileServiceImpl(CommonConfig commonConfig) {
        this.uploadFilePath = commonConfig.getUploadFilePath();
    }

    @Override
    public void delete(String fileName) {
        File file = new File(uploadFilePath + '/' + fileName);
        // 判断文件不为null或文件目录存在
        if (!file.exists()) {
            throw new ClientException("所选文件不存在！");
        }
        try {
            if (file.isFile()){
                file.delete();
            }
            else {
                // 文件夹, 需要先删除文件夹下面所有的文件, 然后删除
                for (File temp : file.listFiles()) {
                    temp.delete();
                }
                file.delete();
            }
        } catch (Exception e) {
            throw new ServerException("删除文件时发生错误：" + e.getMessage());
        }
    }

    @Override
    public void multiUpload(List<FileInfo> files){
        for (FileInfo file : files) {
            upload(file.getBytes(),file.getName());
        }
    }

    public void upload(byte[] file, String fileName){
        log.info("++++++++++++++++++++++++++++++++++进入一次");
        if (Objects.isNull(file)) {
             throw new ClientException("错误！空文件!");
        }
        log.info("上传路径为:{}!", uploadFilePath);
        log.info("上传文件名称为:{}!", fileName);
        File fileTempObj = new File(uploadFilePath + "/" + fileName);
        // 检测目录是否存在
        if (!fileTempObj.getParentFile().exists()) {
            fileTempObj.getParentFile().mkdirs();
        }
        // 使用文件名称检测文件是否已经存在
        if (fileTempObj.exists()) {
            throw new ClientException("文件已存在！");
        }
        try {
            FileUtil.writeBytes(file, fileTempObj);
        } catch (Exception e) {
            throw new ServerException("上传错误：" + e.getMessage());
        }
    }

    @Override
    public void download(HttpServletResponse response, String fileName) {
        File file = new File(uploadFilePath + '/' + fileName);
        if (!file.exists()) {
            throw new ClientException("所选文件不存在！");
        }
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        byte[] readBytes = FileUtil.readBytes(file);
        OutputStream os = null;
        try {
            os = response.getOutputStream();
            os.write(readBytes);
        } catch (IOException e) {
            throw new ServerException("返回文件失败："+e.getMessage());
        }
    }
}
