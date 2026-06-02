package com.personalProject.code_reviewer.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubReviewComment(
        String path,
        String body,
        int position
) {}
