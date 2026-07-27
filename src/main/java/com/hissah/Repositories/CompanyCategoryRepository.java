package com.hissah.Repositories;


import com.hissah.Entities.CompanyCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyCategoryRepository extends JpaRepository<CompanyCategory, Long> {


    List<CompanyCategory> findByCompanyId(Long companyId);


    List<CompanyCategory> findByCategoryId(Long categoryId);

}
