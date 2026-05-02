package com.personalProject.code_reviewer.webhook;

import com.personalProject.code_reviewer.diff.DiffFetcherService;
import lombok.extern.slf4j.Slf4j;
import com.personalProject.code_reviewer.webhook.model.GitHubWebhookPayload;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Slf4j
@RestController
public class WebhookController {

    private final HmacValidator hmacValidator;
    private final ObjectMapper objectMapper;
    private final DiffFetcherService diffFetcherService;

    public WebhookController(HmacValidator hmacValidator, ObjectMapper objectMapper, DiffFetcherService diffFetcherService) {
        this.hmacValidator = hmacValidator;
        this.objectMapper = objectMapper;
        this.diffFetcherService= diffFetcherService;
    }

    @PostMapping("/webhook/github")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signatureHeader, @RequestBody String rawPayload) {
        String rawDiff;
        GitHubWebhookPayload payload;

        if (!hmacValidator.isValidSignature(rawPayload, signatureHeader)) {
            log.warn("Invalid or missing webhook signature. Request rejected.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
        }

        try {
            payload = objectMapper.readValue(rawPayload, GitHubWebhookPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse webhook payload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON payload");
        }

        if (payload.pullRequest() == null || payload.repositoryData() == null) {
            log.warn("Webhook received but pull_request or repository is null. Skipping.");
            return ResponseEntity.ok("Webhook acknowledged but ignored");
        }

        String payloadAction= payload.action();
        int payloadPullRequestNumber= payload.pullRequest().number();
        String payloadPullRequestDiffUrl= payload.pullRequest().diffUrl();
        String payloadRepositoryName= payload.repositoryData().fullName();
        log.info("Webhook received | Action: {} | PR #{} | Repo: {} | Diff URL: {}",
                payloadAction, payloadPullRequestNumber, payloadRepositoryName, payloadPullRequestDiffUrl);

        rawDiff= diffFetcherService.fetchDiff(payloadPullRequestDiffUrl);
        if (rawDiff == null) {
            log.error("Failed to fetch diff for PR #{}. Aborting.", payloadPullRequestNumber);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch diff");
        }

        log.info("Diff fetched successfully for PR #{} | Size: {} chars", payloadPullRequestNumber, rawDiff.length());
        log.debug("Raw diff:\n{}", rawDiff);

        return ResponseEntity.ok("Webhook received");
    }

}
