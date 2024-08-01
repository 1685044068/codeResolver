package com.icbc.codeResolver.utils;

import com.icbc.codeResolver.exception.ClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 文件相关的工具类
 * @author zmq
 * @date 2024/07/31
 */
public class FileUtils {

    /**
     * 将controller接收的MultipartFile，转化为service层容易处理的FileInfo对象
     * @param files controller接收的文件流
     * @return {@link List}<{@link FileInfo}>
     */
    public static List<FileInfo> multipartFilesToFileInfo(MultipartFile[] files) {
        List<FileInfo> fileInfos = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                fileInfos.add(new FileInfo(file.getBytes(),file.getOriginalFilename()));
            } catch (IOException e) {
                throw new ClientException("上传的文件解析失败，请确定文件是否正确");
            }
        }
        return fileInfos;
    }
}
