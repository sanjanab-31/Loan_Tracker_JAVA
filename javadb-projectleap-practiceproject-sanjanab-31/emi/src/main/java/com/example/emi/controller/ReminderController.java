package com.example.emi.controller;
import com.example.emi.scheduler.EMIReminderScheduler;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/reminders")
public class ReminderController {
    private final EMIReminderScheduler scheduler;
    public ReminderController(EMIReminderScheduler scheduler) {
        this.scheduler = scheduler;
    }
    @PostMapping("/send")
    public String sendRemindersNow() {
        scheduler.sendReminders();
        return "✅ Reminder emails triggered manually.";
    }
}
