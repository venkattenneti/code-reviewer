package com.personalProject.code_reviewer.review.dto;

public record ReviewCommentResponse(
        String file,
        String severity,
        String suggestion) {}
