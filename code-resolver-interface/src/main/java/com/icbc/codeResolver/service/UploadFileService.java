package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.Result;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {
    Result upload(MultipartFile file) throws JSONException;

    Result upload(byte[] file,String fileName,String suffixName) throws JSONException;
}
