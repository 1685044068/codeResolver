package com.icbc.coderesolvergitoperationserver;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableDubbo
@SpringBootApplication
@EnableDiscoveryClient
@EnableAspectJAutoProxy
public class CodeResolverGitOperationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeResolverGitOperationServerApplication.class, args);
    }

}
