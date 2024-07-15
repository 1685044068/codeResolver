package com.icbc.codeResolver;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo
@SpringBootApplication
public class CodeResolverClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeResolverClientApplication.class, args);
    }
}
