package com.docuflow.reviewer.service;

import com.docuflow.document.model.DocumentMetadata;
import com.docuflow.document.repository.DocumentMetadataRepository;
import com.docuflow.exception.DocumentNotFoundException;
import com.docuflow.reviewer.dto.ReviewerDocumentResponse;
import com.docuflow.reviewer.model.ReviewAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles Reviewer-specific business logic.
 */
@Service
public class ReviewerService {

    @Autowired
    private DocumentMetadataRepository metadataRepository;

    /**
     * ✅ Fetch all documents with status 'Submitted'
     */
    public List<ReviewerDocumentResponse> getPendingDocuments() {
        List<DocumentMetadata> docs = metadataRepository.findByStatus("Submitted");

        return docs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Approve a document
     */
    public void approveDocument(Long id, String reviewer, String remarks) {
        DocumentMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        if (!metadata.getStatus().equalsIgnoreCase("Submitted")) {
            throw new RuntimeException("Only 'Submitted' documents can be approved.");
        }

        metadata.setStatus("Approved");
        metadata.setWorkflowState("Approved by Reviewer");
        metadata.setRemarks(remarks != null ? remarks : "Approved");
        metadataRepository.save(metadata);

        logReviewAction(reviewer, id, "APPROVED", remarks);
    }

    /**
     * ✅ Reject a document
     */
    public void rejectDocument(Long id, String reviewer, String remarks) {
        DocumentMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        if (!metadata.getStatus().equalsIgnoreCase("Submitted")) {
            throw new RuntimeException("Only 'Submitted' documents can be rejected.");
        }

        metadata.setStatus("Rejected");
        metadata.setWorkflowState("Rejected by Reviewer");
        metadata.setRemarks(remarks != null ? remarks : "Rejected");
        metadataRepository.save(metadata);

        logReviewAction(reviewer, id, "REJECTED", remarks);
    }

    /**
     * ✅ Fetch documents already reviewed by the current reviewer
     */
    public List<ReviewerDocumentResponse> getDocumentsReviewedBy(String reviewer) {
        // If we had a ReviewAction table, this would fetch based on reviewer
        List<DocumentMetadata> reviewedDocs = metadataRepository.findByStatus("Approved");
        reviewedDocs.addAll(metadataRepository.findByStatus("Rejected"));

        return reviewedDocs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Map MySQL entity to response DTO
    private ReviewerDocumentResponse mapToResponse(DocumentMetadata doc) {
        return new ReviewerDocumentResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getOwner(),
                doc.getStatus(),
                doc.getWorkflowState(),
                doc.getRemarks(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    // Optional audit log (for future Pulsar event publishing)
    private void logReviewAction(String reviewer, Long documentId, String action, String remarks) {
        ReviewAction audit = new ReviewAction(reviewer, documentId, action, remarks);
        // Save to DB later if needed
        System.out.println("Audit Log → " + audit);
    }
}
