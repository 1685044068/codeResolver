package com.icbc.codeResolver;

import com.alibaba.fastjson2.JSON;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import static com.alibaba.fastjson2.JSONWriter.Feature.LargeObject;

@EnableDubbo
@SpringBootApplication
public class CodeResolverConsumerApplication {

    public static void main(String[] args) {
        JSON.config(LargeObject, true);

        SpringApplication.run(CodeResolverConsumerApplication.class, args);

    }

}
