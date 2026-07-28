package com.hissah.Entities;

import com.hissah.Enums.DocumentType;
import com.hissah.Enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
@Entity @Table(name = "company_documents")
@Data
public class CompanyDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    private String documentNumber;
    private String filePath;
    private LocalDate expiryDate;
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;
    @Column(name = "company_id", nullable = false)
    private Long companyId;
}
