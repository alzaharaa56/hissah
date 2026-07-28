package com.hissah.Repositories;

import com.hissah.Entities.CompanyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyDocumentRepository extends JpaRepository<CompanyDocument, Long> {


    @Query("SELECT cd FROM CompanyDocument cd WHERE cd.companyId = :companyId")
    List<CompanyDocument> findDocumentsByCompanyId(@Param("companyId") Long companyId);

}
