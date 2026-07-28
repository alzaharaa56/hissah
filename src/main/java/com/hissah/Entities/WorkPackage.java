package com.hissah.Entities;

import com.hissah.Enums.EligibilityType;
import com.hissah.Enums.WorkPackageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
// @NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "work_packages", indexes = {
        @Index(name = "idx_work_package_status", columnList = "status"),
        @Index(name = "idx_work_package_deadline", columnList = "deadline"),
        @Index(name = "idx_work_package_project", columnList = "project_id"),
        @Index(name = "idx_work_package_category", columnList = "category_id")
})
public class WorkPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", nullable = false, unique = true, length = 40)
    private String referenceNumber;

    @Column(nullable = false, length = 180)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String scope;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "budget_min", nullable = false, precision = 15, scale = 3)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", nullable = false, precision = 15, scale = 3)
    private BigDecimal budgetMax;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(nullable = false, length = 120)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WorkPackageStatus status = WorkPackageStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "eligibility_type", nullable = false, length = 30)
    private EligibilityType eligibilityType = EligibilityType.OPEN;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "workPackage", cascade = CascadeType.ALL)
    private List<Bid> bids = new ArrayList<>();

    @OneToOne(mappedBy = "workPackage", fetch = FetchType.LAZY)
    private Award award;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    public WorkPackage() {
    }

    @PrePersist
    void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (status == null) status = WorkPackageStatus.DRAFT;
        if (eligibilityType == null) eligibilityType = EligibilityType.OPEN;
        if (active == null) active = true;
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addBid(Bid bid) {
        bids.add(bid);
        bid.setWorkPackage(this);
    }

}
