package com.icbc.codeResolver;


import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.icbc")
@EnableDubbo
@SpringBootApplication
public class CodeResolverJoernServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeResolverJoernServerApplication.class, args);
    }
}
