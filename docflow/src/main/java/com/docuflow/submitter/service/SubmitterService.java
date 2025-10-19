package com.docuflow.submitter.service;

import com.docuflow.document.dto.DocumentResponse;
import com.docuflow.document.dto.DocumentUploadRequest;
import com.docuflow.document.model.DocumentContent;
import com.docuflow.document.model.DocumentMetadata;
import com.docuflow.document.repository.DocumentContentRepository;
import com.docuflow.document.repository.DocumentMetadataRepository;
import com.docuflow.document.service.DocumentService;
import com.docuflow.exception.DocumentNotFoundException;
import com.docuflow.submitter.dto.SubmitterDocumentResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles Submitter-specific business logic and
 * reuses DocumentService for CRUD operations.
 */
@Service
public class SubmitterService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentMetadataRepository metadataRepository;

    @Autowired
    private DocumentContentRepository contentRepository;

    /**
     * ✅ Upload a new document (sets owner automatically)
     */
    public DocumentResponse uploadDocument(DocumentUploadRequest request) throws IOException {
        request.setOwner(request.getOwner());
        return documentService.uploadDocument(request);
    }

    /**
     * ✅ Upload a new document to reviewer as submit (sets owner automatically)
     */
    public DocumentResponse uploadDocumentassubmit(DocumentUploadRequest request) throws IOException {
        request.setOwner(request.getOwner());
        return documentService.uploadDocumentassubmit(request);
    }

    /**
     * ✅ Get all documents belonging to a submitter
     */
    public List<SubmitterDocumentResponse> getDocumentsByOwner(String owner) {
        List<DocumentMetadata> docs = metadataRepository.findByOwner(owner);

        return docs.stream()
                .map(doc -> new SubmitterDocumentResponse(
                        doc.getId(),
                        doc.getTitle(),
                        doc.getStatus(),
                        doc.getWorkflowState(),
                        doc.getRemarks(),
                        doc.getCreatedAt(),
                        doc.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * ✅ Submit a draft document for review
     */
    public void submitDocumentForReview(Long id, String owner) {
        DocumentMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        if (!metadata.getOwner().equals(owner)) {
            throw new RuntimeException("Unauthorized action: You can only submit your own documents.");
        }

        if (!metadata.getStatus().equalsIgnoreCase("Draft")) {
            throw new RuntimeException("Only Draft documents can be submitted.");
        }

        metadata.setStatus("Submitted");
        metadata.setWorkflowState("Pending Review");
        metadataRepository.save(metadata);
    }

    /**
     * ✅ Update draft document (can only modify if still in Draft)
     */
    public DocumentResponse updateDraftDocument(Long id, DocumentUploadRequest request) throws IOException {
        DocumentMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        if (!metadata.getOwner().equals(request.getOwner())) {
            throw new RuntimeException("Unauthorized: You can only update your own documents.");
        }

        if (!metadata.getStatus().equalsIgnoreCase("Draft")) {
            throw new RuntimeException("You can only update documents in Draft status.");
        }

        // Reuse DocumentService for actual update
        return documentService.updateDocument(id, null, request.getFile());
    }

    public void deletedraftdocument(Long id){

        try{
            documentService.deleteDocument(id);
        }catch (Exception e){
            throw new DocumentNotFoundException("Document not found with ID: " + id);
        }
    }

    public DocumentContent viewdocument(Long id){
        try {
            Optional<DocumentContent> content = contentRepository.findByDocumentId(id);
            DocumentContent documentContent=new DocumentContent();
            documentContent.setDocumentId(content.get().getDocumentId());
            documentContent.setId(content.get().getId());
            documentContent.setFileName(content.get().getFileName());
            documentContent.setFileType(content.get().getFileType());
            documentContent.setFileData(content.get().getFileData());
            documentContent.setFileSize(content.get().getFileSize());
            return documentContent;
        } catch (Exception e) {
            throw new DocumentNotFoundException("content not found with Id:" +id);
        }

    }

}
