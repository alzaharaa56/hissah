package com.hissah.Repositories;

import com.hissah.Entities.Milestone;
import com.hissah.Enums.MilestoneStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    Optional<Milestone> findByIdAndActiveTrue(Long id);
    List<Milestone> findByAwardIdAndActiveTrueOrderByDueDateAsc(Long awardId);
    List<Milestone> findByDueDateBeforeAndStatusInAndActiveTrue(
            LocalDate date, Collection<MilestoneStatus> statuses);
    boolean existsByAwardIdAndStatusNotAndActiveTrue(
            Long awardId, MilestoneStatus status);
    long countByStatusAndActiveTrue(MilestoneStatus status);
    long countByAwardBidBidderCompanyIdAndStatusAndActiveTrue(
            Long subcontractorCompanyId, MilestoneStatus status);
    long countByAwardWorkPackageProjectContractorCompanyIdAndStatusAndActiveTrue(
            Long contractorCompanyId, MilestoneStatus status);
}
