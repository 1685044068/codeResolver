package com.icbc.codeResolver;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@ComponentScan("com.icbc")
public class CodeResolverParserApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeResolverParserApplication.class,args);
    }
}
