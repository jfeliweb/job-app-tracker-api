package com.jobtracker.api.controller;

import com.jobtracker.api.dto.CoverLetterRequest;
import com.jobtracker.api.service.OpenAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/cover-letter")
@RequiredArgsConstructor
public class CoverLetterController {

    private final OpenAiService openAiService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generate(
            @Valid @RequestBody CoverLetterRequest request) {

        String coverLetter = openAiService.generateCoverLetter(
            request.getJobDescription(),
            request.getAdditionalContext()
        );

        return ResponseEntity.ok(Map.of("coverLetter", coverLetter));
    }
}