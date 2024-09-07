package com.icbc.codeResolver.service;



import com.icbc.codeResolver.utils.FileInfo;
import jakarta.servlet.http.HttpServletResponse;


import java.io.IOException;
import java.util.List;

public interface FileService {

    void delete(String fileName);

    void multiUpload(List<FileInfo> files);

    void download(HttpServletResponse response,String fileName) throws IOException;

}
