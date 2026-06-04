package com.personalProject.code_reviewer.persistence.service;

import com.personalProject.code_reviewer.llm.model.ReviewComment;
import com.personalProject.code_reviewer.persistence.entity.PullRequestReviewEntity;
import com.personalProject.code_reviewer.persistence.entity.ReviewCommentEntity;
import com.personalProject.code_reviewer.persistence.repository.PullRequestReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
