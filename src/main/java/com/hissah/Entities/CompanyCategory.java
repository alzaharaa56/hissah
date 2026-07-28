package com.hissah.Entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "company_categories")
@Data
public class CompanyCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;
}
