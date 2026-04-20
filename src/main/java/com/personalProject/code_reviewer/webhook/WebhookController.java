package com.personalProject.code_reviewer.webhook;

import com.personalProject.code_reviewer.webhook.model.GitHubWebhookPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class WebhookController {

    @PostMapping("/webhook/github")
    public ResponseEntity<String> handleWebhook(@RequestBody GitHubWebhookPayload payload) {
        log.info("PR #{} on repo {} | diff: {} | ACTION: {}",
                payload.pullRequest().number(),
                payload.repositoryData().fullName(),
                payload.pullRequest().diffUrl(),
                payload.action());
        return ResponseEntity.ok("Webhook received");
    }

}
