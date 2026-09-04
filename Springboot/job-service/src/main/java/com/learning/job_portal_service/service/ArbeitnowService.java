package com.learning.job_portal_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.learning.job_portal_service.dto.ArbitnowJobItem;
import com.learning.job_portal_service.dto.ArbitnowMetaData;
import com.learning.job_portal_service.dto.ArbitnowResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArbeitnowService {

    private final WebClient webClient;

    /**
     * fetchs jobs list from open external API and handles 4xx and 5xx error 
     * 
     * @param page
     * @return List<ArbitnowResponse>
     * 
     * Enable with cache which stores jobs with pages numbers
     */
    
    @Cacheable(cacheNames = "arbeitnow-jobs", key = "'p'+ #page")
    public List<ArbitnowJobItem> fetchJobs(int page) {
        ArbitnowResponse arbitnowResponse =  webClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("api/job-board-api")
                .queryParam("page", page)
                .build())
            .retrieve()
            .onStatus( status -> status.is4xxClientError() || status.is5xxServerError(), 
                clientResponse -> clientResponse.bodyToMono(String.class)
                    .map(body -> new RuntimeException(" Arbeitnow API error "+ clientResponse.statusCode() + ": "+ body))
            
            )
            .bodyToMono(ArbitnowResponse.class)
            .block();
        
        if(arbitnowResponse == null || arbitnowResponse.data() == null) {
            log.warn("Empty response from arbeitnow API from page = {}", page);
            return List.of();
        }
        // Optional<ArbitnowResponse> reponseOfTypeOptional = Optional.ofNullable(arbitnowResponse);
        // handle null pointer exception for metaData

        int perPage = Optional.of(arbitnowResponse.metaData())
            .map(ArbitnowMetaData::perPage)
            .orElse(0);
        log.info("Fetched {} jobs from Arbeitnow (page={}, total={})", arbitnowResponse.data().size(), page, perPage );

        return arbitnowResponse.data();
    }
}
