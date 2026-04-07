package com.jobtracker.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ApplicationRequest {

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    private String companyName;
    private String status = "APPLIED";
    private String jobUrl;
    private String notes;
    private LocalDate appliedDate;
}