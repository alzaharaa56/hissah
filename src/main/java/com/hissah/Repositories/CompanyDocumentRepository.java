package com.hissah.Repositories;

import com.hissah.Entities.CompanyDocument;
import com.hissah.Enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyDocumentRepository
        extends JpaRepository<CompanyDocument, Long> {

    List<CompanyDocument> findByCompanyId(Long companyId);

    Optional<CompanyDocument> findByCompanyIdAndDocumentType(
            Long companyId,
            DocumentType documentType
    );

    boolean existsByCompanyIdAndDocumentType(
            Long companyId,
            DocumentType documentType
    );

    @Query("""
            SELECT document
            FROM CompanyDocument document
            WHERE document.companyId = :companyId
            """)
    List<CompanyDocument> findDocumentsByCompanyId(
            @Param("companyId") Long companyId
    );
}