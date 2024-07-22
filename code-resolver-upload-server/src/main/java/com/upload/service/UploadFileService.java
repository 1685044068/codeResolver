package com.upload.service;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

public interface UploadFileService {
    public String upload(MultipartFile file) throws JSONException;
}
