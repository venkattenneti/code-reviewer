package com.personalProject.code_reviewer.webhook.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RepositoryData(
        @JsonProperty("full_name") String fullName) { }
