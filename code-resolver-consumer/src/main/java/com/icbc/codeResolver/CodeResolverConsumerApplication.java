package com.icbc.codeResolver;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.icbc")
@ComponentScan("com.icbc.codeResolver.controller")
@EnableDubbo
@SpringBootApplication
public class CodeResolverConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeResolverConsumerApplication.class, args);
    }

}
