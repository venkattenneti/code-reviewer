package com.personalProject.code_reviewer.persistence.repository;

import com.personalProject.code_reviewer.persistence.entity.PullRequestReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PullRequestReviewRepository extends JpaRepository<PullRequestReviewEntity,Long> {
    boolean existsByCommitSha(String commitSha);
    Optional<PullRequestReviewEntity> findTopByPrNumberOrderByReviewedAtDesc(Integer prNumber);
    Page<PullRequestReviewEntity> findAllByOrderByReviewedAtDesc(Pageable pageable);
}
