package com.jobtracker.api.repository;

import com.jobtracker.api.model.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByUserId(Long userId);
    List<Reminder> findByApplicationId(Long applicationId);

    @Query("SELECT r FROM Reminder r WHERE r.remindAt <= :now AND r.isSent = false")
    List<Reminder> findDueReminders(@Param("now") LocalDateTime now);
}