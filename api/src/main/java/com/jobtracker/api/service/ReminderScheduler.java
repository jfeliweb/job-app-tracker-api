package com.jobtracker.api.service;

import com.jobtracker.api.model.Reminder;
import com.jobtracker.api.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderRepository reminderRepository;

    // Runs every 60 seconds
    @Scheduled(fixedRate = 60000)
    public void processDueReminders() {
        List<Reminder> due = reminderRepository.findDueReminders(LocalDateTime.now());

        if (due.isEmpty()) return;

        log.info("Processing {} due reminder(s)", due.size());

        for (Reminder reminder : due) {
            reminder.setIsSent(true);
            reminderRepository.save(reminder);
            log.info("Reminder {} marked sent — application: {}, user: {}",
                reminder.getId(),
                reminder.getApplication().getId(),
                reminder.getUser().getId()
            );
        }
    }
}