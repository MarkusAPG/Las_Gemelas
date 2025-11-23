package com.lasgemelas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /img/** to the uploads directory
        // Using file:/// prefix for absolute file system path
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:///C:/Users/vasqu/OneDrive/Desktop/Las Gemelas/uploads/");
    }
}
