package com.personalProject.code_reviewer.webhook;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebhookController {

    @PostMapping("/webhook/github")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        return ResponseEntity.ok("Webhook received");
    }

}
