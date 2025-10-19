package com.docuflow.document.service;

import com.docuflow.document.dto.DocumentResponse;
import com.docuflow.document.dto.DocumentUploadRequest;
import com.docuflow.document.dto.DocumentUpdateRequest;
import com.docuflow.document.model.DocumentMetadata;
import com.docuflow.document.model.DocumentContent;
import com.docuflow.document.repository.DocumentMetadataRepository;
import com.docuflow.document.repository.DocumentContentRepository;
import com.docuflow.exception.DocumentNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @Autowired
    private DocumentMetadataRepository metadataRepository;

    @Autowired
    private DocumentContentRepository contentRepository;

    /**
     * CREATE - Upload a new document
     */
    public DocumentResponse uploadDocument(DocumentUploadRequest request) throws IOException {

        MultipartFile file = request.getFile();

        // 1️⃣ Save file content in MongoDB
        DocumentContent content = new DocumentContent();
        content.setFileName(file.getOriginalFilename());
        content.setFileType(file.getContentType());
        content.setFileSize(file.getSize());
        content.setFileData(file.getBytes());

        content = contentRepository.save(content);

        // 2️⃣ Save metadata in MySQL
        DocumentMetadata metadata = new DocumentMetadata();
        metadata.setTitle(request.getTitle());
        metadata.setOwner(request.getOwner());
        metadata.setStatus("Draft");
        metadata.setWorkflowState("Created");
        metadata.setMongoFileId(content.getId());
        metadata.setRemarks(request.getRemarks());

        metadata = metadataRepository.save(metadata);

        // 3️⃣ Link back Mongo document with MySQL ID
        content.setDocumentId(metadata.getId());
        contentRepository.save(content);
        // 4️⃣ Build response DTO
        return new DocumentResponse(
                metadata.getId(),
                metadata.getTitle(),
                metadata.getOwner(),
                metadata.getStatus(),
                metadata.getWorkflowState(),
                content.getFileName(),
                content.getFileType(),
                content.getFileSize(),
                metadata.getCreatedAt()
        );
    }
    /**
     * CREATE - Upload a new document as submit;
     */
    public DocumentResponse uploadDocumentassubmit(DocumentUploadRequest request) throws IOException {

        MultipartFile file = request.getFile();

        // 1️⃣ Save file content in MongoDB
        DocumentContent content = new DocumentContent();
        content.setFileName(file.getOriginalFilename());
        content.setFileType(file.getContentType());
        content.setFileSize(file.getSize());
        content.setFileData(file.getBytes());

        content = contentRepository.save(content);

        // 2️⃣ Save metadata in MySQL
        DocumentMetadata metadata = new DocumentMetadata();
        metadata.setTitle(request.getTitle());
        metadata.setOwner(request.getOwner());
        metadata.setStatus("submitted");
        metadata.setWorkflowState("Created");
        metadata.setMongoFileId(content.getId());
        metadata.setRemarks(request.getRemarks());

        metadata = metadataRepository.save(metadata);

        // 3️⃣ Link back Mongo document with MySQL ID
        content.setDocumentId(metadata.getId());
        contentRepository.save(content);
        // 4️⃣ Build response DTO
        return new DocumentResponse(
                metadata.getId(),
                metadata.getTitle(),
                metadata.getOwner(),
                metadata.getStatus(),
                metadata.getWorkflowState(),
                content.getFileName(),
                content.getFileType(),
                content.getFileSize(),
                metadata.getCreatedAt()
        );
    }

    /**
     * READ - Get all documents
     */
    public List<DocumentResponse> getAllDocuments() {
        return metadataRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * READ - Get document by ID
     */
    public DocumentResponse getDocumentById(Long id) {
        DocumentMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        Optional<DocumentContent> contentOpt = contentRepository.findByDocumentId(id);

        DocumentContent content = contentOpt.orElse(null);

        return mapToResponse(metadata, content);
    }

    /**
     * UPDATE - Update metadata or replace file
     */
    public DocumentResponse updateDocument(Long id, DocumentUpdateRequest request, MultipartFile newFile) throws IOException {
        DocumentMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        // Update metadata fields
        if (request.getTitle() != null)
            metadata.setTitle(request.getTitle());
        if (request.getStatus() != null)
            metadata.setStatus(request.getStatus());
        if (request.getRemarks() != null)
            metadata.setRemarks(request.getRemarks());

        // If new file is uploaded, replace in MongoDB
        if (newFile != null && !newFile.isEmpty()) {
            Optional<DocumentContent> oldContentOpt = contentRepository.findByDocumentId(id);
            oldContentOpt.ifPresent(old -> contentRepository.deleteById(old.getId()));

            DocumentContent newContent = new DocumentContent();
            newContent.setDocumentId(id);
            newContent.setFileName(newFile.getOriginalFilename());
            newContent.setFileType(newFile.getContentType());
            newContent.setFileSize(newFile.getSize());
            newContent.setFileData(newFile.getBytes());
            contentRepository.save(newContent);

            metadata.setMongoFileId(newContent.getId());
        }

        metadataRepository.save(metadata);

        return getDocumentById(id);
    }

    /**
     * DELETE - Remove document
     */
    public void deleteDocument(Long id) {
        DocumentMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        // Delete content from MongoDB
        contentRepository.deleteByDocumentId(id);

        // Delete metadata from MySQL
        metadataRepository.deleteById(id);
    }

    // Helper method to map metadata + content into response DTO
    private DocumentResponse mapToResponse(DocumentMetadata metadata) {
        Optional<DocumentContent> contentOpt = contentRepository.findByDocumentId(metadata.getId());
        return mapToResponse(metadata, contentOpt.orElse(null));
    }

    private DocumentResponse mapToResponse(DocumentMetadata metadata, DocumentContent content) {
        return new DocumentResponse(
                metadata.getId(),
                metadata.getTitle(),
                metadata.getOwner(),
                metadata.getStatus(),
                metadata.getWorkflowState(),
                content != null ? content.getFileName() : null,
                content != null ? content.getFileType() : null,
                content != null ? content.getFileSize() : null,
                metadata.getCreatedAt()
        );
    }
}
