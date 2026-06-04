package com.personalProject.code_reviewer.persistence.service;

import com.personalProject.code_reviewer.llm.model.ReviewComment;
import com.personalProject.code_reviewer.persistence.entity.PullRequestReviewEntity;
import com.personalProject.code_reviewer.persistence.entity.ReviewCommentEntity;
import com.personalProject.code_reviewer.persistence.repository.PullRequestReviewRepository;
import com.personalProject.code_reviewer.review.dto.ReviewCommentResponse;
import com.personalProject.code_reviewer.review.dto.ReviewHistoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReviewPersistenceService {

    private final PullRequestReviewRepository reviewRepository;

    public ReviewPersistenceService(PullRequestReviewRepository reviewRepository){
        this.reviewRepository=reviewRepository;
    }

    public boolean isAlreadyReviewed(String commitSha){
        return reviewRepository.existsByCommitSha(commitSha);
    }

    public void saveReview(String repoName,
                           int prNumber,
                           String commitSha,
                           String action,
                           List<ReviewComment> llmComments){
        PullRequestReviewEntity reviewEntity = new PullRequestReviewEntity();

        reviewEntity.setRepoName(repoName);
        reviewEntity.setPrNumber(prNumber);
        reviewEntity.setCommitSha(commitSha);
        reviewEntity.setAction(action);

        List<ReviewCommentEntity> reviewCommentEntities= llmComments.stream()
                .map(reviewComment ->{
                    ReviewCommentEntity reviewCommentEntity = new ReviewCommentEntity();
                    reviewCommentEntity.setFile(reviewComment.file());
                    reviewCommentEntity.setSeverity(reviewComment.severity());
                    reviewCommentEntity.setSuggestion(reviewComment.suggestion());
                    reviewCommentEntity.setReview(reviewEntity);
                    return reviewCommentEntity;
                }).toList();

        reviewEntity.setComments(reviewCommentEntities);
        reviewRepository.save(reviewEntity);
        log.atDebug().log("ReviewPersistenceService.saveReview() | commitSha={} | prNumber={} | comments={}", commitSha, prNumber, reviewCommentEntities.size());
    }

    public Optional<ReviewHistoryResponse> getReviewByPrNumber(Integer prNumber) {
        return reviewRepository
                .findTopByPrNumberOrderByReviewedAtDesc(prNumber)
                .map(this::toResponse);
    }

    public Page<ReviewHistoryResponse> getAllReviews(Pageable pageable) {
        return reviewRepository
                .findAllByOrderByReviewedAtDesc(pageable)
                .map(this::toResponse);
    }

    private ReviewHistoryResponse toResponse(PullRequestReviewEntity entity) {
        List<ReviewCommentResponse> commentResponses = entity.getComments().stream()
                .map(c -> new ReviewCommentResponse(c.getFile(), c.getSeverity(), c.getSuggestion()))
                .toList();

        return new ReviewHistoryResponse(
                entity.getId(),
                entity.getRepoName(),
                entity.getPrNumber(),
                entity.getCommitSha(),
                entity.getAction(),
                entity.getReviewedAt(),
                commentResponses
        );
    }
}
