package com.icbc.codeResolver.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class FileUploadInterceptor implements HandlerInterceptor {

    @Autowired
    CommonConfig commonConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String disableFileTypes =  commonConfig.getDisableFileTypes();
        // 文件上传的Servlet
        if (request instanceof MultipartHttpServletRequest) {
            // 允许所有的文件类型
            if (null == disableFileTypes) {
                return true;
            }

            // 文件后缀类型
            String[] disableType = disableFileTypes.split(",");
            List<String> disableTypeList = Arrays.stream(disableType).collect(Collectors.toList());

            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            Iterator<String> it = multipartRequest.getFileNames();
            if (it != null) {
                while (it.hasNext()) {
                    String fileParameter = it.next();
                    List<MultipartFile> listFile = multipartRequest.getFiles(fileParameter);
                    if (!CollectionUtils.isEmpty(listFile)) {
                        MultipartFile multipartFile = null;
                        String fileName = "";
                        String fileSuffixType = "";

                        for (int i = 0; i < listFile.size(); i++) {
                            // 获取后缀名
                            multipartFile = listFile.get(i);
                            fileName = multipartFile.getOriginalFilename();
                            int indexLocation = 0;
                            if ((indexLocation = fileName.lastIndexOf(".")) > 0) {
                                fileSuffixType = fileName.substring(indexLocation + 1);
                            }

                            // 后缀名检测
                            if (disableTypeList.contains(fileSuffixType)) {
                                response.setCharacterEncoding("UTF-8");
                                ServletOutputStream outputStream = response.getOutputStream();
                                outputStream.write(new String(fileSuffixType + "是不被允许的上传文件类型!").getBytes());
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
