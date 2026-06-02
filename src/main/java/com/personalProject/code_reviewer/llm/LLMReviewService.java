package com.personalProject.code_reviewer.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalProject.code_reviewer.llm.model.ReviewComment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class LLMReviewService {
    public ChatClient chatClient;
    public ObjectMapper objectMapper;

    public LLMReviewService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper){
        this.chatClient= chatClientBuilder.build();
        this.objectMapper= objectMapper;
    }

    public List<ReviewComment> getReview(String prompt){
        log.atDebug().log("LLMReviewService.getReview()- start");
        try{
            String llmResponse= chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            log.atDebug().addArgument(llmResponse).log("LLM Response: {}");
            return objectMapper.readValue(llmResponse,
                    new TypeReference<List<ReviewComment>>() {});
        }catch (JsonProcessingException e){
            log.atError().addArgument(e.getMessage()).log("Failed to Parse LLM Response as Json:{}");
            return Collections.emptyList();
        }catch (Exception e) {
            log.error("LLM call failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
