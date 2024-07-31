package com.icbc.codeResolver.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    public static List<FileInfo> MultiFilesToFileInfo(MultipartFile[] files) throws IOException {
        List<FileInfo> fileInfos = new ArrayList<>();
        for (MultipartFile file : files) {
            fileInfos.add(new FileInfo(file.getBytes(),file.getOriginalFilename()));
        }
        return fileInfos;
    }
}
