package com.routing.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
/* @EnableDiscoveryClient */

public class RoutingEngineApplication extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(RoutingEngineApplication.class, args);
	}

}
