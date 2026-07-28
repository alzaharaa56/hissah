package com.hissah.Repositories;


import com.hissah.Entities.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {


    @Query("SELECT comp FROM Company comp WHERE comp.crNumber = :crNumber")
    Optional<Company> findCompanyByCrNumber(@Param("crNumber") String crNumber);


    @Query("SELECT comp FROM Company comp WHERE comp.userId = :userId")
    Optional<Company> findCompanyByUserId(@Param("userId") Long userId);

}
