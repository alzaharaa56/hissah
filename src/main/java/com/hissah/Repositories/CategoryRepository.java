package com.hissah.Repositories;


import com.hissah.Entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {


    @Query("SELECT c FROM Category c WHERE c.parentCategoryId = :parentCategoryId")
    List<Category> findSubCategoriesByParentId(@Param("parentCategoryId") Long parentCategoryId);


    @Query("SELECT c FROM Category c WHERE c.active = :active")
    List<Category> findCategoriesByActiveStatus(@Param("active") Boolean active);

}