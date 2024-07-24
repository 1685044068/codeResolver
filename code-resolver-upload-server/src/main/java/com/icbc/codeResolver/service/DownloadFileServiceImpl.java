package com.icbc.codeResolver.service;

import cn.hutool.core.io.FileUtil;
import com.icbc.codeResolver.entity.Result;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

@Service
@DubboService(group = "upload")
public class DownloadFileServiceImpl implements DownloadFileService {
    @Value("${file.upload.dir}")
    private String uploadFilePath;
    @Override
    public Result download(HttpServletResponse response, String fileName) throws JSONException, IOException {

            File file = new File(uploadFilePath + '/' + fileName);
            if (!file.exists()) {
                return Result.fail("文件不存在！");
            }

            response.reset();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            response.setContentLength((int) file.length());
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            byte[] readBytes = FileUtil.readBytes(file);
            OutputStream os = response.getOutputStream();
            os.write(readBytes);
            return Result.ok("下载成功");
    }
}
