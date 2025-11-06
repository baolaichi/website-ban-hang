package com.lsb.webshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(exclude = org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class)
@EnableCaching
public class WebShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebShopApplication.class, args);
	}

}
