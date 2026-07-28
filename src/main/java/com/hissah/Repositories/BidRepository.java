package com.hissah.Repositories;

import com.hissah.Entities.Bid;
import com.hissah.Enums.BidStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    Optional<Bid> findByIdAndActiveTrue(Long id);
    Optional<Bid> findByReferenceNumberAndActiveTrue(String referenceNumber);
    boolean existsByBidderCompanyIdAndWorkPackageIdAndStatusInAndActiveTrue(
            Long bidderCompanyId, Long workPackageId, Collection<BidStatus> statuses);
    List<Bid> findByWorkPackageIdAndActiveTrueOrderByAmountAsc(Long workPackageId);
    List<Bid> findByWorkPackageIdAndStatusInAndActiveTrue(
            Long workPackageId, Collection<BidStatus> statuses);
    Page<Bid> findByBidderCompanyIdAndActiveTrueOrderByCreatedAtDesc(
            Long bidderCompanyId, Pageable pageable);
    List<Bid> findTop5ByBidderCompanyIdAndActiveTrueOrderByCreatedAtDesc(
            Long bidderCompanyId);
    List<Bid> findTop5ByWorkPackageProjectContractorCompanyIdAndActiveTrueOrderByCreatedAtDesc(
            Long contractorCompanyId);
    List<Bid> findTop5ByActiveTrueOrderByCreatedAtDesc();
    long countByBidderCompanyIdAndActiveTrue(Long bidderCompanyId);
    long countByBidderCompanyIdAndStatusAndActiveTrue(
            Long bidderCompanyId, BidStatus status);
    long countByStatusAndActiveTrue(BidStatus status);
    long countByWorkPackageProjectContractorCompanyIdAndStatusAndActiveTrue(
            Long contractorCompanyId, BidStatus status);
    long countByWorkPackageProjectContractorCompanyIdAndActiveTrue(
            Long contractorCompanyId);
    long countByWorkPackageIdAndActiveTrue(Long workPackageId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bid b where b.id = :id and b.active = true")
    Optional<Bid> findByIdForUpdate(@Param("id") Long id);
}
