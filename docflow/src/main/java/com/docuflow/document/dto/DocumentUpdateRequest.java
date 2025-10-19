package com.docuflow.document.dto;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO: Represents the incoming request when a user updates a document.
 * This can include metadata updates and/or a new file upload.
 */
public class DocumentUpdateRequest {

    @Size(max = 200, message = "Title should not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Remarks should not exceed 1000 characters")
    private String remarks;

    @Size(max = 50, message = "Status should not exceed 50 characters")
    private String status; // e.g., "Draft", "Submitted", "Approved", "Rejected"

    private MultipartFile file; // Optional new file (for version updates)

    // ✅ Constructors
    public DocumentUpdateRequest() {}

    public DocumentUpdateRequest(String title, String remarks, String status, MultipartFile file) {
        this.title = title;
        this.remarks = remarks;
        this.status = status;
        this.file = file;
    }

    // ✅ Getters & Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public MultipartFile getFile() { return file; }
    public void setFile(MultipartFile file) { this.file = file; }

    // ✅ Utility helper: Check if user uploaded a new file
    public boolean hasNewFile() {
        return file != null && !file.isEmpty();
    }

    // ✅ Utility helper: File size validation
    public boolean isFileTooLarge(long maxSizeInMB) {
        if (file == null) return false;
        long sizeInMB = file.getSize() / (1024 * 1024);
        return sizeInMB > maxSizeInMB;
    }

    // ✅ Utility helper: Allowed file type validation
    public boolean isValidFileType() {
        if (file == null || file.isEmpty()) return true; // No new file = valid
        String type = file.getContentType();
        return type != null && (
                type.equals("application/pdf") ||
                        type.equals("application/msword") ||
                        type.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                        type.startsWith("image/")
        );
    }

    @Override
    public String toString() {
        return "DocumentUpdateRequest{" +
                "title='" + title + '\'' +
                ", remarks='" + remarks + '\'' +
                ", status='" + status + '\'' +
                ", fileName='" + (file != null ? file.getOriginalFilename() : "N/A") + '\'' +
                '}';
    }
}
