package com.personalProject.code_reviewer.webhook;

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

    public WebhookController(HmacValidator hmacValidator, ObjectMapper objectMapper) {
        this.hmacValidator = hmacValidator;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhook/github")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signatureHeader, @RequestBody String rawPayload) {

        if (!hmacValidator.isValidSignature(rawPayload, signatureHeader)) {
            log.warn("Invalid or missing webhook signature. Request rejected.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
        }

        GitHubWebhookPayload payload;

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

        log.info("Webhook received | Action: {} | PR #{} | Repo: {} | Diff URL: {}",
                payload.action(),
                payload.pullRequest().number(),
                payload.repositoryData().fullName(),
                payload.pullRequest().diffUrl());

        return ResponseEntity.ok("Webhook received");
    }

}
