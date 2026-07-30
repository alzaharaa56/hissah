package com.hissah.Repositories;

import com.hissah.Entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndActiveTrue(Long id);

    List<Project> findByContractorCompanyIdAndActiveTrueOrderByCreatedAtDesc(Long contractorCompanyId);

    List<Project> findTop5ByContractorCompanyIdAndActiveTrueOrderByCreatedAtDesc(Long contractorCompanyId);

    List<Project> findTop5ByActiveTrueOrderByCreatedAtDesc();

    long countByContractorCompanyIdAndActiveTrue(Long contractorCompanyId);

    @Query("SELECT p FROM Project p WHERE p.contractorCompanyId = :contractorCompanyId")
    List<Project> findProjectsByContractorCompanyId(@Param("contractorCompanyId") Long contractorCompanyId);
}