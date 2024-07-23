package com.icbc.codeResolver;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDubbo
@ComponentScan("com.icbc")
public class JoernParseApplication {
    public static void main(String[] args) {
        SpringApplication.run(JoernParseApplication.class,args);
    }
}
