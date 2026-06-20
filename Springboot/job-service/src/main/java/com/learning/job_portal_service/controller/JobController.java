package com.learning.job_portal_service.controller;

import java.net.http.HttpClient;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learning.job_portal_service.dto.JobRequest;
import com.learning.job_portal_service.dto.JobResponse;
import com.learning.job_portal_service.service.JobService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController // includes @Controller and @ResponseBody annotations
@RequestMapping("api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    
    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Validated @RequestBody JobRequest jobRequest) {
        JobResponse jobResponse = jobService.create(jobRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(jobResponse);
    }
}
