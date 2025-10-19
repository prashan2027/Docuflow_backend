package com.docuflow.document.controller;

import com.docuflow.document.dto.DocumentUploadRequest;
import com.docuflow.document.dto.DocumentUpdateRequest;
import com.docuflow.document.dto.DocumentResponse;
import com.docuflow.document.service.DocumentService;
import com.docuflow.exception.DocumentNotFoundException;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST Controller for managing document CRUD operations.
 * Connects frontend (React) to the backend service.
 */
@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:5173") // React frontend origin
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    /**
     * ✅ Upload a new document
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @Valid @ModelAttribute DocumentUploadRequest request) throws IOException {

        // Validation: file type and size
        if (!request.isValidFileType()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        if (request.isFileTooLarge(200)) { // limit 200 MB
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(null);
        }

        DocumentResponse response = documentService.uploadDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * ✅ Get all documents
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        List<DocumentResponse> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    /**
     * ✅ Get document by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long id) {
        try {
            DocumentResponse document = documentService.getDocumentById(id);
            return ResponseEntity.ok(document);

        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * ✅ Update document metadata or file
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable Long id,
            @ModelAttribute DocumentUpdateRequest request) throws IOException {

        // Validate optional file
        if (request.hasNewFile() && !request.isValidFileType()) {
            return ResponseEntity.badRequest().body(null);
        }

        if (request.hasNewFile() && request.isFileTooLarge(20)) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(null);
        }

        DocumentResponse updated = documentService.updateDocument(id, request, request.getFile());
        return ResponseEntity.ok(updated);
    }

    /**
     * ✅ Delete document (both MySQL + MongoDB)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok("Document deleted successfully.");
        } catch (DocumentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Document not found with ID: " + id);
        }
    }

    /**
     * ✅ Health check endpoint (optional)
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Document Service is running ✅");
    }
}
