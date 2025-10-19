package com.docuflow.submitter.controller;

import com.docuflow.document.dto.DocumentResponse;
import com.docuflow.document.dto.DocumentUploadRequest;
import com.docuflow.document.model.DocumentContent;
import com.docuflow.document.repository.DocumentContentRepository;
import com.docuflow.exception.DocumentNotFoundException;
import com.docuflow.notify.NotificationService;
import com.docuflow.submitter.dto.SubmitterDocumentResponse;
import com.docuflow.submitter.service.SubmitterService;

import com.sun.net.httpserver.Authenticator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller exposing Submitter-specific endpoints.
 * Accessible only by users with ROLE_SUBMITTER.
 */
@RestController
@RequestMapping("/api/submitter")
public class SubmitterController {

    @Autowired
    private SubmitterService submitterService;

    @Autowired
    private NotificationService notification;

    @Autowired
    private DocumentContentRepository repo;

    /**
     * ✅ Upload a new document as draft  (as logged-in submitter)
     */
    @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @Valid @ModelAttribute DocumentUploadRequest request,
            Authentication authentication) throws IOException {

        String username ="prashant"; // LDAP username
        request.setOwner(username);

        DocumentResponse response = submitterService.uploadDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * ✅ Upload a new document to reviewer as submit  (as logged-in submitter)
     */
    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocumentasssubmit(
            @Valid @ModelAttribute DocumentUploadRequest request,
            Authentication authentication) throws IOException {
        String name=request.getFile().getOriginalFilename();
        String username ="prashant"; // LDAP username
        request.setOwner(username);

        DocumentResponse response = submitterService.uploadDocumentassubmit(request);
        String email="prashantbhusnar05@gmail.com";
         notification.sendDocumentStatusChange(email,name,"submitted");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ✅ Get all documents belonging to logged-in submitter
     */
    @GetMapping("/documents")
    public ResponseEntity<List<SubmitterDocumentResponse>> getMyDocuments(Authentication authentication) {
        String username ="prashant";
        List<SubmitterDocumentResponse> documents = submitterService.getDocumentsByOwner(username);
        return ResponseEntity.ok(documents);
    }

    /**
     * ✅ Submit document for review (change status to 'Submitted')
     */
    @PutMapping("/documents/{id}/submit")
    public ResponseEntity<String> submitForReview(
            @PathVariable Long id,
            Authentication authentication) {

        String username = "prashant";//bypass
        submitterService.submitDocumentForReview(id, username);
      Optional< DocumentContent> doc= repo.findByDocumentId(id);
        String docname=doc.get().getFileName();
        notification.sendDocumentStatusChange("prashantbhusnar05@gmail.com",docname,"submitted document for review");
        return ResponseEntity.ok("Document submitted for review successfully.");
    }

    /**
     * ✅ Update a draft document (change metadata or file)
     */
    @PutMapping(value = "/documents/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> updateDraftDocument(
            @PathVariable Long id,
            @ModelAttribute DocumentUploadRequest request,
            Authentication authentication) throws IOException {

        String username ="prashant";// bypass
        request.setOwner(username);

        DocumentResponse updated = submitterService.updateDraftDocument(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * ✅ delete a  draft document (change metadata or file)
     */
    @DeleteMapping(value = "/documents/{id}")
    public ResponseEntity<?> deleteDraftDocument(
            @PathVariable Long id) throws IOException {

        submitterService.deletedraftdocument(id);
        return ResponseEntity.accepted().build();
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
