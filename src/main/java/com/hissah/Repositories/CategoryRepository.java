package com.hissah.Repositories;


import com.hissah.Entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {


    List<Category> findByParentCategoryId(Long parentCategoryId);


    List<Category> findByActive(Boolean active);

}