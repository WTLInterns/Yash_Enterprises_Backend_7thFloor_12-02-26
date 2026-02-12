package com.company.attendance.crm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.IOException;

@Configuration
public class FreemarkerConfig {

    public FreeMarkerConfigurer freeMarkerConfigurer() throws IOException {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
        configurer.setTemplateLoaderPath("classpath:/templates/");
        configurer.setDefaultEncoding("UTF-8");
        
        // Configure FreeMarker settings
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty("template_update_delay", "0");
        properties.setProperty("default_encoding", "UTF-8");
        properties.setProperty("output_encoding", "UTF-8");
        properties.setProperty("locale", "en_US");
        properties.setProperty("datetime_format", "yyyy-MM-dd HH:mm:ss");
        properties.setProperty("date_format", "yyyy-MM-dd");
        properties.setProperty("time_format", "HH:mm:ss");
        properties.setProperty("number_format", "0.##");
        properties.setProperty("boolean_format", "true,false");
        
        configurer.setFreemarkerSettings(properties);
        return configurer;
    }
}
