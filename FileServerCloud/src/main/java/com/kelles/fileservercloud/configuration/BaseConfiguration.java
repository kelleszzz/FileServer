package com.kelles.fileservercloud.configuration;

import com.google.gson.Gson;
import com.kelles.fileserversdk.setting.Setting;
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
        return Setting.DEFAULT_CHARSET;
    }
}
