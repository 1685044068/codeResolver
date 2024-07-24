package com.icbc.codeResolver.service;

import com.icbc.codeResolver.entity.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.configurationprocessor.json.JSONException;


import java.io.IOException;

public interface DownloadFileService {
    public Result download(HttpServletResponse response, String fileName) throws JSONException, IOException;
}
