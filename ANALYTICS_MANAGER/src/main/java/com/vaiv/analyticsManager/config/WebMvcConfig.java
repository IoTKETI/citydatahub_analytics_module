package com.vaiv.analyticsManager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.vaiv.analyticsManager.common.service.InterceptorService;

@SuppressWarnings("deprecation")
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter{
	
	@Bean
	public InterceptorService interceptor() {
	    return new InterceptorService();
	}
	
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor())
        		.addPathPatterns("/admin/**")
        		.addPathPatterns("/sandbox/**")
        		.addPathPatterns("/project/**")
        		.addPathPatterns("/originalData/**")
				.addPathPatterns("/batch/instances/**")
				.addPathPatterns("/batchLogs/**")
				.addPathPatterns("/batchServiceRequests/**")
				.addPathPatterns("/batchServices/**")
				.addPathPatterns("/batchLogs/**")
                .addPathPatterns("/*");
    }
}
