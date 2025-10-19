package com.docuflow.submitter.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit log model — records every submit action performed by a user.
 */
@Entity
@Table(name = "submitter_action")
public class SubmitterAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private Long documentId;
    private String action; // e.g., "SUBMITTED", "UPDATED"
    private LocalDateTime actionTime;

    public SubmitterAction() {}

    public SubmitterAction(String username, Long documentId, String action) {
        this.username = username;
        this.documentId = documentId;
        this.action = action;
        this.actionTime = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
}
