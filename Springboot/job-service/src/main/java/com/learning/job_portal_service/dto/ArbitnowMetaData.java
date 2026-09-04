package com.learning.job_portal_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ArbitnowMetaData(
    @JsonProperty("current_page") int currentPage,
    @JsonProperty("current_page_url") String currentPageUrl,
    int from,
    String path,
    @JsonProperty("per_page") int perPage,
    int to,
    String terms,
    String info
    
) {

}
