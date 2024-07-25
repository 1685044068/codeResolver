package com.icbc.codeResolver;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableDubbo
@SpringBootApplication
public class CodeResolverUploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeResolverUploadApplication.class, args);
    }

}