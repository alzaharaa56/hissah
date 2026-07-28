package com.hissah.Entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
@Entity @Table(name = "projects")
@Data
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String referenceNumber;
    private String sector;
    private String location;
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    @Column(name = "contractor_company_id", nullable = false)
    private Long contractorCompanyId;
}
