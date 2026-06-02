package com.personalProject.code_reviewer.llm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PromptBuilderService {

    public String buildPrompt(String rawDiff){
        String systemMessage="""
            You are a senior Java and Spring Boot engineer performing a code review.
            Analyse the provided git diff and return ONLY a valid JSON array of review comments.
            Each object must have exactly these three fields:
              - "file"       : the filename being reviewed
              - "severity"   : one of INFO, WARN, ERROR
              - "suggestion" : a clear, actionable suggestion
            Rules:
            - Return ONLY the raw JSON array. No explanation, no markdown, no code fences.
            - If there are no issues found, return an empty array: []
            """;
        String userMessage="Review this diff:\n\n" + rawDiff;
        String resultMessage=systemMessage+"\n"+userMessage;
        //log.atDebug().log(resultMessage);
        return resultMessage;
    }
}
