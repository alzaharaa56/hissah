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
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByIdAndActiveTrue(Long id);

    List<Company> findByVerificationStatusAndActiveTrue(VerificationStatus verificationStatus);

    boolean existsByCrNumberAndActiveTrue(String crNumber);

    boolean existsByUserIdAndActiveTrue(Long userId);

    @Query("SELECT comp FROM Company comp WHERE comp.crNumber = :crNumber")
    Optional<Company> findCompanyByCrNumber(@Param("crNumber") String crNumber);

    @Query("SELECT comp FROM Company comp WHERE comp.userId = :userId")
    Optional<Company> findCompanyByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Company comp SET comp.verificationStatus = :status WHERE comp.id = :companyId")
    int updateVerificationStatus(@Param("companyId") Long companyId, @Param("status") VerificationStatus status);
}
