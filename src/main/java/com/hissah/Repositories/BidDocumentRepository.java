package com.hissah.Repositories;

import com.hissah.Entities.BidDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidDocumentRepository extends JpaRepository<BidDocument, Long> {
    Optional<BidDocument> findByIdAndActiveTrue(Long id);
    List<BidDocument> findByBidIdAndActiveTrueOrderByCreatedAtAsc(Long bidId);
    Optional<BidDocument> findByStoredFileNameAndActiveTrue(String storedFileName);
    long countByBidIdAndActiveTrue(Long bidId);
}
