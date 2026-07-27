package com.hissah.Repositories;


import com.hissah.Entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {


    List<Project> findByContractorCompanyId(Long contractorCompanyId);

}
