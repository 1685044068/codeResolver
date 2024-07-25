package com.icbc.codeResolver.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    String delete(HttpServletResponse response, String fileName) throws JSONException;

    String upload(MultipartFile file) throws JSONException;

    String upload(byte[] file, String fileName, String suffixName) throws JSONException;

    public String download(HttpServletResponse response,String fileName) throws JSONException, IOException;

}
