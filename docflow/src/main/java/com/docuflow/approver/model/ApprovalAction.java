package com.docuflow.approver.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit log entity for approver actions.
 */
@Entity
@Table(name = "approval_action")
public class ApprovalAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String approver;
    private Long documentId;
    private String action;   // FINALIZED / ARCHIVED
    private String remarks;
    private LocalDateTime actionTime;

    public ApprovalAction() {}

    public ApprovalAction(String approver, Long documentId, String action, String remarks) {
        this.approver = approver;
        this.documentId = documentId;
        this.action = action;
        this.remarks = remarks;
        this.actionTime = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getActionTime() { return actionTime; }
    public void setActionTime(LocalDateTime actionTime) { this.actionTime = actionTime; }

    @Override
    public String toString() {
        return "ApprovalAction{" +
                "approver='" + approver + '\'' +
                ", documentId=" + documentId +
                ", action='" + action + '\'' +
                ", remarks='" + remarks + '\'' +
                ", actionTime=" + actionTime +
                '}';
    }
}
