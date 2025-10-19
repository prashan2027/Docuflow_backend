package com.docuflow.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("bhusnarprashant18@gmail.com");
        mailSender.send(message);
    }

    public void sendDocumentStatusChange(String to, String documentName, String status) {
        String subject = "Document Status Update: " + documentName;
        String body = "Dear User,\n\nYour document '" + documentName +
                "' has been updated to status: " + status + ".\n\n" +
                "Thank you,\nDocuFlow System";
        sendEmail(to, subject, body);
    }
}
