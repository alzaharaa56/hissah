package com.hissah.Repositories;


import com.hissah.Entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {


    Optional<Company> findByCrNumber(String crNumber);


    Optional<Company> findByUserId(Long userId);

}
