package com.docuflow.document.repository;

import com.docuflow.document.model.DocumentContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DocumentContentRepository extends MongoRepository<DocumentContent, String> {

    // Find the file content using linked MySQL document ID
    Optional<DocumentContent> findByDocumentId(Long documentId);

    // Delete file data when document is removed
    void deleteByDocumentId(Long documentId);
}
