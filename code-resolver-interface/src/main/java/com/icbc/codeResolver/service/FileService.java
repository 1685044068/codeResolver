package com.icbc.codeResolver.service;


import com.icbc.codeResolver.entity.Result;
import com.icbc.codeResolver.utils.FileInfo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileService {

    void delete(String fileName);

    void multiUpload(List<FileInfo> files);

    void download(HttpServletResponse response,String fileName) throws IOException;

}
