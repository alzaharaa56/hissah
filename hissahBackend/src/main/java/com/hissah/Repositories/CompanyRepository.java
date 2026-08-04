package com.hissah.Repositories;

import com.hissah.Entities.Company;
import com.hissah.Enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository
        extends JpaRepository<Company, Long> {


    List<Company> findByVerificationStatus(
            VerificationStatus verificationStatus
    );

    boolean existsByCrNumberIgnoreCase(String crNumber);

    boolean existsByUserId(Long userId);

    Optional<Company> findByCrNumberIgnoreCase(
            String crNumber
    );

    Optional<Company> findByUserId(Long userId);

    @Modifying
    @Query("""
            UPDATE Company company
            SET company.verificationStatus = :status
            WHERE company.id = :companyId
            """)
    int updateVerificationStatus(
            @Param("companyId") Long companyId,
            @Param("status") VerificationStatus status
    );
}