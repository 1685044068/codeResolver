package com.icbc.codeResolver.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
@DubboService(group = "upload")
public class DeleteFileServiceImpl implements DeleteFileService {
    @Value("${file.upload.dir}")
    private String uploadFilePath;
    @Override
    public String delete(HttpServletResponse response,String fileName) throws JSONException {
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
