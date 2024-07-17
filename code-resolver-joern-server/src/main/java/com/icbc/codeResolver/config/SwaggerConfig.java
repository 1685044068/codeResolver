package com.icbc.codeResolver.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @BelongsProject: code-resolver
 * @BelongsPackage: com.icbc.codeResolver.Config
 * @Author: zero
 * @CreateTime: 2024-07-17  09:50
 * @Description: TODO
 * @Version: 1.0
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI swaggerOpenAPI(){
        return new OpenAPI()
                .info(new Info().title("oa流程控制")
                        .contact(new Contact())
                        .description("我的API文档")
                        .version("版本v.1")
                        .license(new License().name("Apache 2.0").url("https://xxxx.xxx.xxx")));
    }
}

