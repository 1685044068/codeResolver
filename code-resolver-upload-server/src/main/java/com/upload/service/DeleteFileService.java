package com.upload.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.configurationprocessor.json.JSONException;

public interface DeleteFileService {
    public String delete(HttpServletResponse response,String fileName) throws JSONException;
}
