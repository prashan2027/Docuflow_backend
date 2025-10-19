package com.docuflow.document.repository;

import com.docuflow.document.model.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, Long> {

    // Find all documents owned by a specific user
    List<DocumentMetadata> findByOwner(String owner);

    // Search documents by title (case-insensitive)
    List<DocumentMetadata> findByTitleContainingIgnoreCase(String title);

    // Find by status (e.g., "Submitted", "Approved")
    List<DocumentMetadata> findByStatus(String status);
}
