package com.icbc.codeResolver.service;


import com.icbc.codeResolver.entity.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    Result delete(HttpServletResponse response, String fileName) throws JSONException;

    Result upload(MultipartFile file) throws JSONException;

    Result upload(byte[] file, String fileName, String suffixName) throws JSONException;

    Result download(HttpServletResponse response,String fileName) throws JSONException, IOException;

}
