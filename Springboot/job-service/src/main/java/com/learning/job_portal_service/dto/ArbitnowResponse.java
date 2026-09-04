package com.learning.job_portal_service.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ArbitnowResponse(
    @JsonProperty("data") List<ArbitnowJobItem> data,
    @JsonProperty("meta") ArbitnowMetaData metaData
) {

}
