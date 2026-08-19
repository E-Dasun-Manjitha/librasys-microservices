package com.librasys.notification.controller;

import com.librasys.notification.dto.NotificationRequest;
import com.librasys.notification.model.Notification;
import com.librasys.notification.repository.NotificationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notify")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // POST /api/notify/email - send email notification (simulated)
    @PostMapping("/email")
    public ResponseEntity<Notification> sendEmail(@Valid @RequestBody NotificationRequest request) {
        Notification notification = new Notification(
                request.getMemberId(), "EMAIL", request.getMessage());

        Notification saved = notificationRepository.save(notification);

        // Simulate sending email — just log to console
        System.out.println(">>> [EMAIL] Notification sent to member " +
                request.getMemberId() + ": " + request.getMessage());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // POST /api/notify/due-reminder - send due date reminder
    @PostMapping("/due-reminder")
    public ResponseEntity<Notification> sendDueReminder(
            @Valid @RequestBody NotificationRequest request) {
        Notification notification = new Notification(
                request.getMemberId(), "DUE_REMINDER", request.getMessage());

        Notification saved = notificationRepository.save(notification);

        System.out.println(">>> [DUE_REMINDER] Notification sent to member " +
                request.getMemberId() + ": " + request.getMessage());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/notify/history/{memberId} - get notification history
    @GetMapping("/history/{memberId}")
    public List<Notification> getNotificationHistory(@PathVariable String memberId) {
        return notificationRepository.findByMemberIdOrderBySentAtDesc(memberId);
    }
}
