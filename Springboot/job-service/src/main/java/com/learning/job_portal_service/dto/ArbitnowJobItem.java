package com.learning.job_portal_service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ArbitnowJobItem(
    String slug,
    @JsonProperty("company_name") String companyName,
    String title,
    String description,
    boolean remote,
    String url,
    List<String> tags,
    @JsonProperty("job_types") List<String> jobTypes,
    String location,
    String createdAt
) {

}
