package com.personalProject.code_reviewer.diff;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class DiffFetcherService {

    public final RestClient restClient;

    public DiffFetcherService(RestClient restClient) {
        this.restClient = restClient;
    }

    public String fetchDiff(String diffUrl){
        log.atDebug().log("DiffFetcherService.fetchDiff() - start");
        String rawDiff;
        try {
            rawDiff = restClient.get().uri(diffUrl).retrieve().body(String.class);
        }catch(RestClientException e){
            log.atError().addArgument(e.getMessage()).log("Failed to fetch diff from GitHub:{}");
            log.error("Full error: ", e);
            return null;
        }
        log.atDebug().addArgument(rawDiff).log("DiffFetcherService.fetchDiff() - end | rawDiff: {}");
        return rawDiff;
    }
}