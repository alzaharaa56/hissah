package com.hissah.Repositories;


import com.hissah.Entities.CompanyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyCategoryRepository extends JpaRepository<CompanyCategory, Long> {

    @Query("SELECT cc FROM CompanyCategory cc WHERE cc.companyId = :companyId")
    List<CompanyCategory> findCategoriesByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT cc FROM CompanyCategory cc WHERE cc.categoryId = :categoryId")
    List<CompanyCategory> findCompaniesByCategoryId(@Param("categoryId") Long categoryId);

}