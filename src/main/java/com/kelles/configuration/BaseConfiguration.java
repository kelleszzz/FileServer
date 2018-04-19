package com.kelles.configuration;

import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;

@Configuration
public class BaseConfiguration {
    @Bean(value = "gson")
    public Gson gson(){
        return new Gson();
    }

    @Bean(value = "defaultCharset")
    public Charset defaultCharset(){
        return Charset.forName("utf-8");
    }
}
