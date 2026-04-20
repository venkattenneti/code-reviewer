package com.personalProject.code_reviewer.webhook.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubWebhookPayload (
        @JsonProperty("action") String action,
        @JsonProperty("pull_request") PullRequestData pullRequest,
        @JsonProperty("repository") RepositoryData repositoryData){ }
