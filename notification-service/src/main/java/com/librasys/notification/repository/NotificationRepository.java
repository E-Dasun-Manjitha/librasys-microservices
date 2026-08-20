package com.librasys.notification.repository;

import com.librasys.notification.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByMemberIdOrderBySentAtDesc(String memberId);
}
