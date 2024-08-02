package com.icbc.codeResolver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    private CorsConfiguration buildConfig(){
        CorsConfiguration corsconfiguration = new CorsConfiguration();
        corsconfiguration.addAllowedOrigin("*");
        corsconfiguration.addAllowedHeader("*");
        corsconfiguration.addAllowedMethod("*");
        return corsconfiguration;
    }

    @Bean
    public CorsFilter corsFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //配置所有请求
        source.registerCorsConfiguration("/**", buildConfig());
        return new CorsFilter(source);

    }
}