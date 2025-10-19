package com.docuflow.approver.controller;

import com.docuflow.approver.dto.ApproverDocumentResponse;
import com.docuflow.approver.service.ApproverService;
import com.docuflow.document.model.DocumentContent;
import com.docuflow.exception.DocumentNotFoundException;
import com.docuflow.submitter.service.SubmitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Approver operations.
 * Accessible only by ROLE_APPROVER.
 */
@RestController
@RequestMapping("/api/approver")
public class ApproverController {

    @Autowired
    private ApproverService approverService;

    @Autowired
    private SubmitterService submitterService;

    /**
     * ✅ Fetch all documents waiting for approval (status = "Approved")
     */
    @GetMapping("/documents/pending")
    public ResponseEntity<List<ApproverDocumentResponse>> getDocumentsPendingFinalization() {
        List<ApproverDocumentResponse> docs = approverService.getDocumentsPendingFinalization();
        return ResponseEntity.ok(docs);
    }

    /**
     * ✅ Finalize a document (mark as Finalized)
     */
    @PutMapping("/documents/{id}/finalize")
    public ResponseEntity<String> finalizeDocument(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(required = false) String remarks) {

        String approver ="alice";
        approverService.finalizeDocument(id, approver, remarks);
        return ResponseEntity.ok("Document finalized successfully by " + approver);
    }

    /**
     * ✅ Reject  a document (mark as Rejected)
     */
    @PutMapping("/documents/{id}/reject")
    public ResponseEntity<String> rejectDocument(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(required = false) String remarks) {

        String approver ="alice";


        approverService.rejectDocument(id, approver, remarks);

        return ResponseEntity.ok("Document finalized successfully by " + approver);
    }

    /**
     * ✅ Archive a document (optional)
     */
    @PutMapping("/documents/{id}/archive")
    public ResponseEntity<String> archiveDocument(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(required = false) String remarks) {

        String approver ="alice";
        approverService.archiveDocument(id, approver, remarks);
        return ResponseEntity.ok("Document archived successfully by " + approver);
    }

    /**
     * ✅ Fetch all finalized documents
     */
    @GetMapping("/documents/finalized")
    public ResponseEntity<List<ApproverDocumentResponse>> getFinalizedDocuments() {
        List<ApproverDocumentResponse> docs = approverService.getFinalizedDocuments();
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
