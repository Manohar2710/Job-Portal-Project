package com.learning.job_portal_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.learning.job_portal_service",
    "com.learning.common"
})
public class JobPortalServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobPortalServiceApplication.class, args);
	}

}
