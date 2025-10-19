package com.docuflow.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO: Represents the incoming request when a user uploads a document.
 * Combines metadata (title, owner, remarks) with the actual file.
 */
public class DocumentUploadRequest {

    @NotBlank(message = "Document title is required")
    @Size(max = 200, message = "Title should not exceed 200 characters")
    private String title;

    @NotBlank(message = "Owner (uploader username) is required")
    private String owner;  // This can be auto-fetched from LDAP in real scenarios

    @Size(max = 1000, message = "Remarks should not exceed 1000 characters")
    private String remarks;

    @NotNull(message = "File must be provided")
    private MultipartFile file;

    // ✅ Default Constructor
    public DocumentUploadRequest() {}

    // ✅ All-Args Constructor (optional for testing or service-level usage)
    public DocumentUploadRequest(String title, String owner, String remarks, MultipartFile file) {
        this.title = title;
        this.owner = owner;
        this.remarks = remarks;
        this.file = file;
    }

    // ✅ Getters & Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    // ✅ Utility Validation Method (optional but useful)
    public boolean isValidFileType() {
        if (file == null || file.isEmpty()) return false;
        String type = file.getContentType();
        return type != null && (
                type.equals("application/pdf") ||
                        type.equals("application/msword") ||
                        type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                        type.startsWith("image/")
        );
    }

    public boolean isFileTooLarge(long maxSizeInMB) {
        if (file == null) return true;
        long fileSizeInMB = file.getSize() / (1024 * 1024);
        return fileSizeInMB > maxSizeInMB;
    }

    @Override
    public String toString() {
        return "DocumentUploadRequest{" +
                "title='" + title + '\'' +
                ", owner='" + owner + '\'' +
                ", remarks='" + remarks + '\'' +
                ", fileName='" + (file != null ? file.getOriginalFilename() : "N/A") + '\'' +
                '}';
    }
}
