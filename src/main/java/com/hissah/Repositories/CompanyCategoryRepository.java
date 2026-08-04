package com.hissah.Repositories;

import com.hissah.Entities.CompanyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyCategoryRepository
        extends JpaRepository<CompanyCategory, Long> {

    List<CompanyCategory> findByCompanyId(Long companyId);

    List<CompanyCategory> findByCategoryId(Long categoryId);

    boolean existsByCompanyIdAndCategoryId(
            Long companyId,
            Long categoryId
    );

    void deleteByCompanyIdAndCategoryId(
            Long companyId,
            Long categoryId
    );

    @Query("""
            SELECT relation
            FROM CompanyCategory relation
            WHERE relation.companyId = :companyId
            """)
    List<CompanyCategory> findCategoriesByCompanyId(
            @Param("companyId") Long companyId
    );

    @Query("""
            SELECT relation
            FROM CompanyCategory relation
            WHERE relation.categoryId = :categoryId
            """)
    List<CompanyCategory> findCompaniesByCategoryId(
            @Param("categoryId") Long categoryId
    );
}