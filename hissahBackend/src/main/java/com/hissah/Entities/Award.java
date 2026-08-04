package com.hissah.Entities;

import com.hissah.Enums.AwardStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@Table(name = "awards", uniqueConstraints = {
        @UniqueConstraint(name = "uk_award_work_package", columnNames = "work_package_id"),
        @UniqueConstraint(name = "uk_award_bid", columnNames = "bid_id")
})
public class Award {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", nullable = false, unique = true, length = 40)
    private String referenceNumber;

    @Column(name = "awarded_at", nullable = false)
    private LocalDateTime awardedAt;

    @Column(name = "agreed_amount", nullable = false, precision = 15, scale = 3)
    private BigDecimal agreedAmount;

    @Column(name = "agreed_delivery_days", nullable = false)
    private Integer agreedDeliveryDays;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AwardStatus status = AwardStatus.ACTIVE;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bid_id", nullable = false, unique = true)
    private Bid bid;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_package_id", nullable = false, unique = true)
    private WorkPackage workPackage;

    @OneToMany(mappedBy = "award", cascade = CascadeType.ALL)
    private List<Milestone> milestones = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    public Award() {
    }

    @PrePersist
    void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        if (awardedAt == null) awardedAt = now;
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (status == null) status = AwardStatus.ACTIVE;
        if (active == null) active = true;
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addMilestone(Milestone milestone) {
        milestones.add(milestone);
        milestone.setAward(this);
    }


}
