package com.jobtracker.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateCoverLetter(String jobDescription, String additionalContext) {
        String prompt = buildPrompt(jobDescription, additionalContext);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
            "model", model,
            "max_tokens", 1000,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            "https://api.openai.com/v1/chat/completions",
            HttpMethod.POST,
            entity,
            Map.class
        );

        // Dig into the response structure to extract the text
        List<Map> choices = (List<Map>) response.getBody().get("choices");
        Map message = (Map) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private String buildPrompt(String jobDescription, String additionalContext) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Write a professional cover letter for the following job description.\n\n");
        prompt.append("Job Description:\n").append(jobDescription).append("\n\n");
        if (additionalContext != null && !additionalContext.isBlank()) {
            prompt.append("Additional Context:\n").append(additionalContext).append("\n\n");
        }
        prompt.append("Write a compelling, concise cover letter of 3-4 paragraphs.");
        return prompt.toString();
    }
}