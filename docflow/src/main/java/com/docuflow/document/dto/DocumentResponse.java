package com.docuflow.document.dto;

import java.time.LocalDateTime;

/**
 * DTO: Represents the response sent to the client (frontend)
 * after document operations like upload, fetch, or update.
 */
public class DocumentResponse {

    private Long id;                 // MySQL ID (metadata)
    private String title;            // Document title
    private String owner;            // LDAP username or uploader
    private String status;           // Draft / Submitted / Approved
    private String workflowState;    // Current step in workflow
    private String remarks;          // Optional notes
    private String fileName;         // File name from MongoDB
    private String fileType;         // MIME type
    private Long fileSize;           // File size (bytes)
    private String mongoFileId;      // Reference to MongoDB _id
    private Integer version;         // Document version
    private LocalDateTime createdAt; // Creation time
    private LocalDateTime updatedAt; // Last modification time

    // ✅ Constructors
    public DocumentResponse() {}

    public DocumentResponse(Long id, String title, String owner, String status,
                            String workflowState, String fileName, String fileType,
                            Long fileSize, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.status = status;
        this.workflowState = workflowState;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    // ✅ Full Constructor for flexible mapping
    public DocumentResponse(Long id, String title, String owner, String status,
                            String workflowState, String remarks, String fileName,
                            String fileType, Long fileSize, String mongoFileId,
                            Integer version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        this.status = status;
        this.workflowState = workflowState;
        this.remarks = remarks;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.mongoFileId = mongoFileId;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ✅ Getters & Setters
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

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMongoFileId() { return mongoFileId; }
    public void setMongoFileId(String mongoFileId) { this.mongoFileId = mongoFileId; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ✅ Helper: Convert file size to human-readable format
    public String getReadableFileSize() {
        if (fileSize == null) return "0 B";
        double size = fileSize;
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    @Override
    public String toString() {
        return "DocumentResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", owner='" + owner + '\'' +
                ", status='" + status + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + getReadableFileSize() +
                ", createdAt=" + createdAt +
                '}';
    }
}
