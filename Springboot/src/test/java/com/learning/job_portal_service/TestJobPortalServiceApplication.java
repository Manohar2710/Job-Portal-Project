package com.learning.job_portal_service;

import org.springframework.boot.SpringApplication;

public class TestJobPortalServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(JobPortalServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
