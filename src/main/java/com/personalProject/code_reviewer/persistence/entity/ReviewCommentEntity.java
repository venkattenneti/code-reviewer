package com.personalProject.code_reviewer.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "review_comments")
public class ReviewCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private PullRequestReviewEntity review;

    @Column(nullable = false)
    private String file;

    @Column(nullable = false)
    private String severity;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String suggestion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PullRequestReviewEntity getReview() {
        return review;
    }

    public void setReview(PullRequestReviewEntity review) {
        this.review = review;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }
}