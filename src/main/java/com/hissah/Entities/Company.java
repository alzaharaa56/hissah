package com.hissah.Entities;

import com.hissah.Enums.CompanyType;
import com.hissah.Enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity @Table(name = "companies")
@Data
public class Company {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String legalName;
    private String tradingName;
    @Enumerated(EnumType.STRING)
    private CompanyType companyType;
    @Column(unique = true, nullable = false)
    private String crNumber;
    private String governorate;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;
}
