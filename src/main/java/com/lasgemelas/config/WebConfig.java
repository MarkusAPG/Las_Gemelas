package com.lasgemelas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /product-images/** to the uploads directory
        registry.addResourceHandler("/product-images/**")
                .addResourceLocations("file:///C:/Users/vasqu/OneDrive/Desktop/Las Gemelas/uploads/");
    }
}
