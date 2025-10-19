package com.docuflow.document.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "document_content")
public class DocumentContent {

    @Id
    private String id;  // MongoDB _id

    @Field("document_id")
    private Long documentId;  // MySQL DocumentMetadata.id link

    private String fileName;

    private String fileType;  // MIME type, e.g., application/pdf

    private Long fileSize;

    private byte[] fileData;  // actual binary content

    // Constructors
    public DocumentContent() {}
    public DocumentContent(Long documentId, String fileName, String fileType, Long fileSize, byte[] fileData) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileData = fileData;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }
}
