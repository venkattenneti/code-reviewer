package com.personalProject.code_reviewer.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubReviewRequest(
        String body,
        String event,
        @JsonProperty("comments")List<GitHubReviewComment> comments
) {}
