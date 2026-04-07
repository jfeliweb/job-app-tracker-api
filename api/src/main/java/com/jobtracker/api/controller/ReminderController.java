package com.jobtracker.api.controller;

import com.jobtracker.api.model.Reminder;
import com.jobtracker.api.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public ResponseEntity<List<Reminder>> getAll(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(reminderService.getForUser(user.getUsername()));
    }

    @PostMapping
    public ResponseEntity<Reminder> create(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails user) {

        Long applicationId = Long.valueOf(body.get("applicationId").toString());
        LocalDateTime remindAt = LocalDateTime.parse(body.get("remindAt").toString());
        String message = (String) body.get("message");

        return ResponseEntity.ok(
            reminderService.create(applicationId, user.getUsername(), remindAt, message)
        );
    }
}