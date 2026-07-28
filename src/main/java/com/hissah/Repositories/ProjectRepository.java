package com.hissah.Repositories;


import com.hissah.Entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {


    @Query("SELECT p FROM Project p WHERE p.contractorCompanyId = :contractorCompanyId")
    List<Project> findProjectsByContractorCompanyId(@Param("contractorCompanyId") Long contractorCompanyId);

}

