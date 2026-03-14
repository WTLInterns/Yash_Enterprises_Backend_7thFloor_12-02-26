package com.company.attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files from C:/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:C:/uploads/");
        
        // Legacy support for old uploads folder
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
