package com.icbc.codeResolver.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
@Data
public class CommonConfig {

    @Value("${file.upload.disableTypes}")
    private String disableFileTypes;

    @Value("${file.upload.dir}")
    private String uploadFilePath;

}
