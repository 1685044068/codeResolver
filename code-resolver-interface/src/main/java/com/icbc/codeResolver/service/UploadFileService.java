package com.icbc.codeResolver.service;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {
    String upload(MultipartFile file) throws JSONException;

    String upload(byte[] file,String fileName,String suffixName) throws JSONException;
}
