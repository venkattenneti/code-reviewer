package com.personalProject.code_reviewer.webhook.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PullRequestData(
        @JsonProperty("number") int number,
        @JsonProperty("diff_url") String diffUrl ) { }
