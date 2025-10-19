package com.docuflow.reviewer.controller;

import com.docuflow.document.model.DocumentContent;
import com.docuflow.document.model.DocumentMetadata;
import com.docuflow.document.repository.DocumentContentRepository;
import com.docuflow.document.repository.DocumentMetadataRepository;
import com.docuflow.exception.DocumentNotFoundException;
import com.docuflow.notify.NotificationService;
import com.docuflow.reviewer.dto.ReviewerDocumentResponse;
import com.docuflow.reviewer.service.ReviewerService;
import com.docuflow.submitter.service.SubmitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Reviewer operations.
 * Accessible only by ROLE_REVIEWER.
 */
@RestController
@RequestMapping("/api/reviewer")
public class ReviewerController {

    @Autowired
    private ReviewerService reviewerService;

    @Autowired
    private NotificationService notify;


    private DocumentContent documentContent=new DocumentContent();

    @Autowired
    private DocumentMetadataRepository docrepo;

    @Autowired
    private DocumentContentRepository doccontentrepo;

    @Autowired
    public SubmitterService submitterService;



    /**
     * ✅ Fetch all documents waiting for review
     */
    @GetMapping("/documents/pending")
    public ResponseEntity<List<ReviewerDocumentResponse>> getPendingDocuments() {
        List<ReviewerDocumentResponse> docs = reviewerService.getPendingDocuments();
        return ResponseEntity.ok(docs);
    }

    /**
     * ✅ Approve a document
     */
    @PutMapping("/documents/{id}/approve")
    public ResponseEntity<String> approveDocument(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(required = false) String remarks) {

        String reviewer ="alice";
        reviewerService.approveDocument(id, reviewer, remarks);
             Optional<DocumentMetadata> data=docrepo.findById(id);
            String owner= data.get().getOwner();
            owner="prashantbhusnar05@gmail.com";
            Optional<DocumentContent> content=doccontentrepo.findByDocumentId(id);
            String filename=content.get().getFileName();
        notify.sendDocumentStatusChange( owner,filename,"approved document by "+reviewer);
        return ResponseEntity.ok("Document approved successfully by " + reviewer);
    }

    /**
     * ✅ Reject a document
     */
    @PutMapping("/documents/{id}/reject")
    public ResponseEntity<String> rejectDocument(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(required = false) String remarks) {

        String reviewer ="alice";
        reviewerService.rejectDocument(id, reviewer, remarks);
        Optional<DocumentMetadata> data=docrepo.findById(id);
        String owner= data.get().getOwner();
        owner="prashantbhusnar05@gmail.com";
        Optional<DocumentContent> content=doccontentrepo.findByDocumentId(id);
        String filename=content.get().getFileName();
        notify.sendDocumentStatusChange( owner,filename,"approved document by "+reviewer);
        return ResponseEntity.ok("Document rejected successfully by " + reviewer);
    }

    /**
     * ✅ Get all reviewed documents (by this reviewer)
     */
    @GetMapping("/documents/reviewed")
    public ResponseEntity<List<ReviewerDocumentResponse>> getReviewedDocuments(Authentication authentication) {
        String reviewer ="alice";
        List<ReviewerDocumentResponse> docs = reviewerService.getDocumentsReviewedBy(reviewer);
        return ResponseEntity.ok(docs);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> viewdocument(@PathVariable Long id){
        try{
            DocumentContent doc = submitterService.viewdocument(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + doc.getFileName() + "\"") // Changed to inline
                    .body(doc.getFileData());
        } catch(Exception e){
            throw new DocumentNotFoundException("content not found");
        }
    }
}
