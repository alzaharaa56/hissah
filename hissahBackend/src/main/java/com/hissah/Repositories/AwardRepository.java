package com.hissah.Repositories;

import com.hissah.Entities.Award;
import com.hissah.Enums.AwardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AwardRepository extends JpaRepository<Award, Long> {
    Optional<Award> findByIdAndActiveTrue(Long id);
    Optional<Award> findByWorkPackageIdAndActiveTrue(Long workPackageId);
    Optional<Award> findByBidIdAndActiveTrue(Long bidId);
    boolean existsByWorkPackageIdAndActiveTrue(Long workPackageId);
    List<Award> findByBidBidderCompanyIdAndActiveTrueOrderByAwardedAtDesc(
            Long subcontractorCompanyId);
    List<Award> findByWorkPackageProjectContractorCompanyIdAndActiveTrueOrderByAwardedAtDesc(
            Long contractorCompanyId);
    List<Award> findTop5ByBidBidderCompanyIdAndActiveTrueOrderByAwardedAtDesc(
            Long subcontractorCompanyId);
    List<Award> findTop5ByWorkPackageProjectContractorCompanyIdAndActiveTrueOrderByAwardedAtDesc(
            Long contractorCompanyId);
    List<Award> findTop5ByActiveTrueOrderByAwardedAtDesc();
    long countByStatusAndActiveTrue(AwardStatus status);
    long countByBidBidderCompanyIdAndStatusAndActiveTrue(
            Long subcontractorCompanyId, AwardStatus status);
    long countByWorkPackageProjectContractorCompanyIdAndStatusAndActiveTrue(
            Long contractorCompanyId, AwardStatus status);
}
