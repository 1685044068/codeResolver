package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.configurationprocessor.json.JSONException;


public interface DeleteFileService {
    public Result delete(HttpServletResponse response, String fileName) throws JSONException;
}
