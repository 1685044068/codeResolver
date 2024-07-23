package com.icbc.codeResolver.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.configurationprocessor.json.JSONException;


import java.io.IOException;

public interface DownloadFileService {
    public String download(HttpServletResponse response,String fileName) throws JSONException, IOException;
}
