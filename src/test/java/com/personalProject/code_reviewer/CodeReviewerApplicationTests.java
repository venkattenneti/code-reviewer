package com.personalProject.code_reviewer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"app.github.webhook-secret=test-secret",
		"app.github.token=test-token",
		"spring.ai.openai.api-key=test-key"
})
class CodeReviewerApplicationTests {
	@Test
	void contextLoads() {
	}
}
