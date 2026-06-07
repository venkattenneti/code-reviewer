package com.personalProject.code_reviewer.review.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewHistoryResponse(
        Long id,
        String repoName,
        Integer prNumber,
        String commitSha,
        String action,
        LocalDateTime reviewedAt,
        List<ReviewCommentResponse> comments
) {}
