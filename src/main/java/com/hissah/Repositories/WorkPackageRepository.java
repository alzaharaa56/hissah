package com.hissah.Repositories;

import com.hissah.Entities.WorkPackage;
import com.hissah.Enums.WorkPackageStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkPackageRepository extends JpaRepository<WorkPackage, Long>,
        JpaSpecificationExecutor<WorkPackage> {

    Optional<WorkPackage> findByIdAndActiveTrue(Long id);
    Optional<WorkPackage> findByReferenceNumberAndActiveTrue(String referenceNumber);
    boolean existsByProjectIdAndTitleIgnoreCaseAndActiveTrue(Long projectId, String title);
    List<WorkPackage> findByProjectIdAndActiveTrueOrderByCreatedAtDesc(Long projectId);
    Page<WorkPackage> findByProjectContractorCompanyIdAndActiveTrueOrderByCreatedAtDesc(
            Long contractorCompanyId, Pageable pageable);
    List<WorkPackage> findTop5ByStatusAndActiveTrueOrderByPublishedAtDesc(
            WorkPackageStatus status);
    List<WorkPackage> findTop5ByProjectContractorCompanyIdAndActiveTrueOrderByCreatedAtDesc(
            Long contractorCompanyId);
    List<WorkPackage> findTop5ByActiveTrueOrderByCreatedAtDesc();
    List<WorkPackage> findByDeadlineBeforeAndStatusAndActiveTrue(
            LocalDateTime deadline, WorkPackageStatus status);
    long countByStatusAndActiveTrue(WorkPackageStatus status);
    long countByProjectContractorCompanyIdAndActiveTrue(Long contractorCompanyId);
    long countByProjectContractorCompanyIdAndStatusAndActiveTrue(
            Long contractorCompanyId, WorkPackageStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wp from WorkPackage wp where wp.id = :id and wp.active = true")
    Optional<WorkPackage> findByIdForUpdate(@Param("id") Long id);
}
