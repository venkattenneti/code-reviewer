package com.personalProject.code_reviewer.review;

import com.personalProject.code_reviewer.diff.DiffFetcherService;
import com.personalProject.code_reviewer.llm.LLMReviewService;
import com.personalProject.code_reviewer.llm.PromptBuilderService;
import com.personalProject.code_reviewer.llm.model.ReviewComment;
import com.personalProject.code_reviewer.persistence.service.ReviewPersistenceService;
import com.personalProject.code_reviewer.review.dto.ReviewHistoryResponse;
import com.personalProject.code_reviewer.review.dto.TriggerReviewRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewPersistenceService reviewPersistenceService;
    private final DiffFetcherService diffFetcherService;
    private final PromptBuilderService promptBuilderService;
    private final LLMReviewService llmReviewService;

    public ReviewController(ReviewPersistenceService reviewPersistenceService, DiffFetcherService diffFetcherService,
                            PromptBuilderService promptBuilderService, LLMReviewService llmReviewService){
        this.reviewPersistenceService= reviewPersistenceService;
        this.diffFetcherService=diffFetcherService;
        this.promptBuilderService=promptBuilderService;
        this.llmReviewService=llmReviewService;
    }

    @GetMapping("/{prNumber}")
    public ResponseEntity<ReviewHistoryResponse> getReviewByPr(@PathVariable Integer prNumber){
        log.atDebug().addArgument(prNumber).log("ReviewController.getReviewByPr(): PR Number:{}");
        return reviewPersistenceService.getReviewByPrNumber(prNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ReviewHistoryResponse>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        Pageable pageable= PageRequest.of(page,size);
        log.atDebug().addArgument(page).addArgument(size).log("ReviewController.getHistory(): Page:{},Size:{}");
        return ResponseEntity.ok(reviewPersistenceService.getAllReviews(pageable));
    }

    @PostMapping("/trigger")
    public ResponseEntity<String> triggerReview(@RequestBody TriggerReviewRequest request) {
        log.atDebug().log("Manual trigger | prNumber={} | commitSha={}", request.prNumber(), request.commitSha());

        if (reviewPersistenceService.isAlreadyReviewed(request.commitSha())) {
            log.atDebug().log("CommitSha={} already reviewed — skipping", request.commitSha());
            return ResponseEntity.ok("Already reviewed — skipped");
        }

        String rawDiff = diffFetcherService.fetchDiff(request.diffUrl());
        if (rawDiff == null) {
            log.atError().log("Failed to fetch diff for diffUrl={}", request.diffUrl());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch diff");
        }

        String prompt = promptBuilderService.buildPrompt(rawDiff);
        List<ReviewComment> comments = llmReviewService.getReview(prompt);

        reviewPersistenceService.saveReview(
                request.repoName(),
                request.prNumber(),
                request.commitSha(),
                "manual-trigger",
                comments
        );

        log.atDebug().log("Manual review complete | prNumber={} | comments={}", request.prNumber(), comments.size());
        return ResponseEntity.ok("Review triggered by API and saved");
    }

}
