package com.personalProject.code_reviewer.persistence.repository;


import com.personalProject.code_reviewer.persistence.entity.PullRequestReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PullRequestReviewRepository extends JpaRepository<PullRequestReviewEntity,Long> {
    boolean existsByCommitSha(String commitSha);
}
