package com.vaiv.analyticsManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@SpringBootApplication
@EnableZuulProxy
@EnableScheduling
public class AnalyticsManagerApplication  extends SpringBootServletInitializer{

	@Override protected SpringApplicationBuilder
	  configure(SpringApplicationBuilder application) { return
	  application.sources(AnalyticsManagerApplication.class); }
	
	public static void main(String[] args) {
		SpringApplication.run(AnalyticsManagerApplication.class, args);
	}

}
