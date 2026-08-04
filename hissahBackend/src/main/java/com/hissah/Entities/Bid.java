package com.hissah.Entities;

import com.hissah.Enums.BidStatus;
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
@Table(name = "bids", indexes = {
        @Index(name = "idx_bid_package", columnList = "work_package_id"),
        @Index(name = "idx_bid_company", columnList = "bidder_company_id"),
        @Index(name = "idx_bid_status", columnList = "status")
})
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", nullable = false, unique = true, length = 40)
    private String referenceNumber;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal amount;

    @Column(name = "delivery_days", nullable = false)
    private Integer deliveryDays;

    @Lob
    @Column(name = "proposal_text", nullable = false, columnDefinition = "TEXT")
    private String proposalText;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private BidStatus status = BidStatus.DRAFT;

    @Column(name = "decision_reason", length = 500)
    private String decisionReason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bidder_company_id", nullable = false)
    private Company bidderCompany;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_package_id", nullable = false)
    private WorkPackage workPackage;

    @OneToMany(mappedBy = "bid", cascade = CascadeType.ALL)
    private List<BidDocument> documents = new ArrayList<>();

    @OneToOne(mappedBy = "bid", fetch = FetchType.LAZY)
    private Award award;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    public Bid() {
    }

    @PrePersist
    void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (status == null) status = BidStatus.DRAFT;
        if (active == null) active = true;
    }

    @PreUpdate
    void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addDocument(BidDocument document) {
        documents.add(document);
        document.setBid(this);
    }


}
