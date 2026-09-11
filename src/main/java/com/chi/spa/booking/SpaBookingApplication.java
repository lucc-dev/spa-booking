package com.chi.spa.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpaBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpaBookingApplication.class, args);
	}

}