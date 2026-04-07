package com.jobtracker.api.service;

import com.jobtracker.api.dto.ApplicationRequest;
import com.jobtracker.api.dto.ApplicationResponse;
import com.jobtracker.api.model.Application;
import com.jobtracker.api.model.Company;
import com.jobtracker.api.model.User;
import com.jobtracker.api.repository.ApplicationRepository;
import com.jobtracker.api.repository.CompanyRepository;
import com.jobtracker.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public List<ApplicationResponse> getAll(String email) {
        User user = getUser(email);
        return applicationRepository.findByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ApplicationResponse create(ApplicationRequest request, String email) {
        User user = getUser(email);

        Application app = new Application();
        app.setUser(user);
        app.setJobTitle(request.getJobTitle());
        app.setStatus(request.getStatus());
        app.setJobUrl(request.getJobUrl());
        app.setNotes(request.getNotes());
        app.setAppliedDate(request.getAppliedDate());

        // Look up or create the company
        if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
            Company company = companyRepository
                    .findByUserId(user.getId())
                    .stream()
                    .filter(c -> c.getName().equalsIgnoreCase(request.getCompanyName()))
                    .findFirst()
                    .orElseGet(() -> {
                        Company newCompany = new Company();
                        newCompany.setName(request.getCompanyName());
                        newCompany.setUser(user);
                        return companyRepository.save(newCompany);
                    });
            app.setCompany(company);
        }

        return toResponse(applicationRepository.save(app));
    }

    public ApplicationResponse update(Long id, ApplicationRequest request, String email) {
        User user = getUser(email);

        // findByIdAndUserId ensures users can only edit their own applications
        Application app = applicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Application not found"));

        app.setJobTitle(request.getJobTitle());
        app.setStatus(request.getStatus());
        app.setJobUrl(request.getJobUrl());
        app.setNotes(request.getNotes());
        app.setAppliedDate(request.getAppliedDate());
        app.setUpdatedAt(LocalDateTime.now());

        return toResponse(applicationRepository.save(app));
    }

    public void delete(Long id, String email) {
        User user = getUser(email);
        Application app = applicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Application not found"));
        applicationRepository.delete(app);
    }

    // Private helpers

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private ApplicationResponse toResponse(Application app) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(app.getId());
        response.setJobTitle(app.getJobTitle());
        response.setStatus(app.getStatus());
        response.setJobUrl(app.getJobUrl());
        response.setNotes(app.getNotes());
        response.setAppliedDate(app.getAppliedDate());
        response.setCreatedAt(app.getCreatedAt());
        response.setUpdatedAt(app.getUpdatedAt());
        if (app.getCompany() != null) {
            response.setCompanyName(app.getCompany().getName());
        }
        return response;
    }
}