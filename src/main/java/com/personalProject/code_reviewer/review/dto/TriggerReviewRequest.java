package com.personalProject.code_reviewer.review.dto;

public record TriggerReviewRequest(
        String diffUrl,
        String repoName,
        Integer prNumber,
        String commitSha
) {}