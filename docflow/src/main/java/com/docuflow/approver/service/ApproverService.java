package com.docuflow.approver.service;

import com.docuflow.document.model.DocumentContent;
import com.docuflow.document.model.DocumentMetadata;
import com.docuflow.document.repository.DocumentContentRepository;
import com.docuflow.document.repository.DocumentMetadataRepository;
import com.docuflow.exception.DocumentNotFoundException;
import com.docuflow.approver.dto.ApproverDocumentResponse;
import com.docuflow.approver.model.ApprovalAction;

import com.docuflow.notify.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles Approver-specific business logic.
 */
@Service
public class ApproverService {

    @Autowired
    private DocumentMetadataRepository metadataRepository;

    @Autowired
    private NotificationService notify;

    @Autowired
    private DocumentContentRepository doccontent;

    /**
     * ✅ Fetch all documents approved by Reviewer (status = "Approved")
     */
    public List<ApproverDocumentResponse> getDocumentsPendingFinalization() {
        List<DocumentMetadata> docs = metadataRepository.findByStatus("Approved");
        return docs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Finalize a document (marks workflow complete)
     */
    public void finalizeDocument(Long id, String approver, String remarks) {
        DocumentMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        if (!metadata.getStatus().equalsIgnoreCase("Approved")) {
            throw new RuntimeException("Only 'Approved' documents can be finalized.");
        }

        metadata.setStatus("Finalized");
        metadata.setWorkflowState("Finalized by Approver");
        metadata.setRemarks(remarks != null ? remarks : "Finalized");
        metadataRepository.save(metadata);
        String owner=metadata.getOwner();
        owner="prashantbhusnar05@gmail.com";
        Optional<DocumentContent>  doc=doccontent.findByDocumentId(id);
        String  filename=doc.get().getFileName();

        notify.sendDocumentStatusChange( owner,filename,"Finalized by Approver ");


        logApprovalAction(approver, id, "FINALIZED", remarks);
    }
    /**
     * ✅ Reject a document (marks workflow rejected)
     */
    public void rejectDocument(Long id, String approver, String remarks) {
        DocumentMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        if (!metadata.getStatus().equalsIgnoreCase("Approved")) {
            throw new RuntimeException("Only 'Approved' documents can be finalized.");
        }

        metadata.setStatus("Rejected");
        metadata.setWorkflowState("Rejected by Approver");
        metadata.setRemarks(remarks != null ? remarks :"Rejected by alice");
        metadataRepository.save(metadata);
        String owner=metadata.getOwner();
        owner="prashantbhusnar05@gmail.com";
        Optional<DocumentContent>  doc=doccontent.findByDocumentId(id);
        String  filename=doc.get().getFileName();

        notify.sendDocumentStatusChange( owner,filename,"rejected by Approver ");


        logApprovalAction(approver, id, "FINALIZED", remarks);
    }

    /**
     * ✅ Archive a document (optional)
     */
    public void archiveDocument(Long id, String approver, String remarks) {
        DocumentMetadata metadata = metadataRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));

        if (!metadata.getStatus().equalsIgnoreCase("Finalized")) {
            throw new RuntimeException("Only finalized documents can be archived.");
        }

        metadata.setStatus("Archived");
        metadata.setWorkflowState("Archived by Approver");
        metadata.setRemarks(remarks != null ? remarks : "Archived");
        metadataRepository.save(metadata);

        logApprovalAction(approver, id, "ARCHIVED", remarks);
    }

    /**
     * ✅ Get all finalized documents
     */
    public List<ApproverDocumentResponse> getFinalizedDocuments() {
        List<DocumentMetadata> docs = metadataRepository.findByStatus("Finalized");
        return docs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Map entity to DTO
    private ApproverDocumentResponse mapToResponse(DocumentMetadata doc) {
        return new ApproverDocumentResponse(
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

    // Optional audit log (or Pulsar event)
    private void logApprovalAction(String approver, Long docId, String action, String remarks) {
        ApprovalAction audit = new ApprovalAction(approver, docId, action, remarks);
        System.out.println("Audit Log → " + audit);
    }
}
