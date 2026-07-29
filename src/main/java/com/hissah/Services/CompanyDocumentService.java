package com.hissah.Services;

import com.hissah.DTO.Request.CompanyDocumentRequestDTO;
import com.hissah.DTO.Response.CompanyDocumentResponseDTO;
import com.hissah.Enums.Role;

import java.nio.file.Path;
import java.util.List;

public interface CompanyDocumentService {

    CompanyDocumentResponseDTO upload(
            Long companyId,
            CompanyDocumentRequestDTO request
    );

    List<CompanyDocumentResponseDTO> getForCompany(
            Long companyId,
            Long currentCompanyId,
            Role currentRole
    );

    DownloadFile download(
            Long documentId,
            Long currentCompanyId,
            Role currentRole
    );

    void delete(
            Long documentId,
            Long currentCompanyId
    );

    record DownloadFile(
            Path path,
            String fileName,
            String contentType,
            long fileSize
    ) {
    }
}
