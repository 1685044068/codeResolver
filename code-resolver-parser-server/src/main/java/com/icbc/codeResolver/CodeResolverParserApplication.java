package com.icbc.codeResolver;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
@EnableAspectJAutoProxy
public class CodeResolverParserApplication {
    public static void main(String[] args) {
        SpringApplication.run(CodeResolverParserApplication.class,args);
    }
}
