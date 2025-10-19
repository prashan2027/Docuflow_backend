package com.docuflow.reviewer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Simple audit log model for review actions.
 */
@Entity
@Table(name = "review_action")
public class ReviewAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reviewer;
    private Long documentId;
    private String action;   // APPROVED / REJECTED
    private String remarks;
    private LocalDateTime actionTime;

    public ReviewAction() {}

    public ReviewAction(String reviewer, Long documentId, String action, String remarks) {
        this.reviewer = reviewer;
        this.documentId = documentId;
        this.action = action;
        this.remarks = remarks;
        this.actionTime = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }
}
