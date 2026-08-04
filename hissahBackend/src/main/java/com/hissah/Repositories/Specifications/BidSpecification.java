package com.hissah.Repositories.Specifications;


import com.hissah.Entities.Project;
import org.springframework.data.jpa.domain.Specification;

public class BidSpecification {


    public static Specification<Project> hasTitle(String title) {
        return (root, query, criteriaBuilder) -> {
            if (title == null || title.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }


    public static Specification<Project> hasCompanyId(Long companyId) {
        return (root, query, criteriaBuilder) -> {
            if (companyId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("company").get("id"), companyId);
        };
    }


    public static Specification<Project> hasStatus(String status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null || status.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), status.toLowerCase());
        };
    }
}