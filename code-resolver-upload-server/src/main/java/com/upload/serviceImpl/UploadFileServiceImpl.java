package com.upload.serviceImpl;

import cn.hutool.core.io.FileUtil;
//import com.common.CSVService;
import com.upload.service.UploadFileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;


@Service
@Slf4j
public class UploadFileServiceImpl implements UploadFileService {
//    @Autowired
//    private AsyncServiceImpl asyncService;
//    @DubboReference
//    private CSVService csvService;
    @Value("${file.upload.dir}")
    private String uploadFilePath;
    @Override
    public String upload(MultipartFile file) throws JSONException {
        JSONObject result = new JSONObject();
        if (file.isEmpty()) {
            result.put("error", "空文件!");
            return result.toString();
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
            result.put("error", "文件已经存在!");
            return result.toString();
        }

        try {
            // 写入文件:方式1
            // file.transferTo(fileTempObj);
            // 写入文件:方式2
            FileUtil.writeBytes(file.getBytes(), fileTempObj);
        } catch (Exception e) {
            log.error("发生错误: {}", e);
            result.put("error", e.getMessage());
            return result.toString();
        }
        result.put("success,存储路径为：", uploadFilePath + "/" + fileName);
//        csvService.transmit();
//        asyncService.asyncTransmit();
        String s=result.toString().replace("\\","");
        return s;
    }

}
