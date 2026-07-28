package com.hissah.Services;

import com.hissah.DTOs.Requests.BidDecisionRequestDTO;
import com.hissah.DTOs.Requests.BidRequestDTO;
import com.hissah.DTOs.Responses.BidComparisonResponseDTO;
import com.hissah.DTOs.Responses.BidResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BidService {
    BidResponseDTO createDraft(
            BidRequestDTO request, Long currentCompanyId, Long currentUserId);

    BidResponseDTO submitNew(
            BidRequestDTO request, Long currentCompanyId, Long currentUserId);

    BidResponseDTO update(
            Long bidId,
            BidRequestDTO request,
            Long currentCompanyId,
            Long currentUserId);

    BidResponseDTO submitDraft(
            Long bidId, Long currentCompanyId, Long currentUserId);

    BidResponseDTO withdraw(
            Long bidId, Long currentCompanyId, Long currentUserId);

    BidResponseDTO decide(
            Long bidId,
            BidDecisionRequestDTO request,
            Long contractorCompanyId,
            Long currentUserId);

    BidResponseDTO getById(Long bidId, Long currentCompanyId);

    Page<BidResponseDTO> getMyBids(
            Long currentCompanyId, Pageable pageable);

    List<BidComparisonResponseDTO> compareForWorkPackage(
            Long workPackageId, Long contractorCompanyId);
}
