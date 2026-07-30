/*
 * Commit message: feat(repository): add dynamic work package opportunity filters
 */
package com.hissah.Repositories.Specifications;

import com.hissah.DTO.Request.WorkPackageSearchRequestDTO;
import com.hissah.Entities.WorkPackage;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class WorkPackageSpecification {

    private WorkPackageSpecification() {
    }

    public static Specification<WorkPackage> from(WorkPackageSearchRequestDTO request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String value = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")), value),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("scope")), value),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("referenceNumber")), value)
                ));
            }

            if (request.getProjectId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("project", JoinType.INNER).get("id"),
                        request.getProjectId()));
            }
            if (request.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.join("category", JoinType.INNER).get("id"),
                        request.getCategoryId()));
            }
            if (request.getLocation() != null && !request.getLocation().isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("location")),
                        "%" + request.getLocation().trim().toLowerCase() + "%"));
            }
            if (request.getBudgetMin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("budgetMax"), request.getBudgetMin()));
            }
            if (request.getBudgetMax() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("budgetMin"), request.getBudgetMax()));
            }
            if (request.getDeadlineFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("deadline"), request.getDeadlineFrom()));
            }
            if (request.getDeadlineTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("deadline"), request.getDeadlineTo()));
            }
            if (request.getEligibilityType() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("eligibilityType"), request.getEligibilityType()));
            }
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("status"), request.getStatus()));
            }

            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
