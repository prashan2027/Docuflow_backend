package com.docuflow.approver.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for Approver dashboard.
 */
public class ApproverDocumentResponse {

    private Long id;
    private String title;
    private String owner;
    private String status;
    private String workflowState;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApproverDocumentResponse() {}

    public ApproverDocumentResponse(Long id, String title, String owner, String status,
                                    String workflowState, String remarks,
                                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.status = status;
        this.workflowState = workflowState;
        this.remarks = remarks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWorkflowState() { return workflowState; }
    public void setWorkflowState(String workflowState) { this.workflowState = workflowState; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
