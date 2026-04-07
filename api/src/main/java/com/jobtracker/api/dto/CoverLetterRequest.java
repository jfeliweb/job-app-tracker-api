package com.jobtracker.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CoverLetterRequest {

    @NotBlank(message = "Job description is required")
    private String jobDescription;

    private String additionalContext;
}