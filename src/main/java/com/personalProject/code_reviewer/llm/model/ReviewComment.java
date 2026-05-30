package com.personalProject.code_reviewer.llm.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReviewComment(
        String file,
        String severity,
        String suggestion
) {}
