package com.jobtracker.api.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ApplicationResponse {
    private Long id;
    private String jobTitle;
    private String companyName;
    private String status;
    private String jobUrl;
    private String notes;
    private LocalDate appliedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}