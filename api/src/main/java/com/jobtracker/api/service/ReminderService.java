package com.jobtracker.api.service;

import com.jobtracker.api.model.Application;
import com.jobtracker.api.model.Reminder;
import com.jobtracker.api.model.User;
import com.jobtracker.api.repository.ApplicationRepository;
import com.jobtracker.api.repository.ReminderRepository;
import com.jobtracker.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public List<Reminder> getForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return reminderRepository.findByUserId(user.getId());
    }

    public Reminder create(Long applicationId, String email,
                           LocalDateTime remindAt, String message) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Application application = applicationRepository
                .findByIdAndUserId(applicationId, user.getId())
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Reminder reminder = new Reminder();
        reminder.setUser(user);
        reminder.setApplication(application);
        reminder.setRemindAt(remindAt);
        reminder.setMessage(message);

        return reminderRepository.save(reminder);
    }
}