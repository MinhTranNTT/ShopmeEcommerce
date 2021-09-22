package com.shopme.admin.category;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.shopme.common.entity.Category;

public interface CategoryRepository extends PagingAndSortingRepository<Category, Integer>{

	@Query("SELECT c FROM Category c WHERE c.parent.id IS NULL")
	public List<Category> findRootCategories(Sort sort);

	@Query("SELECT c FROM Category c WHERE c.parent.id IS NULL")
	public Page<Category> findRootCategories(Pageable page);
	
	@Query("SELECT c FROM Category c WHERE c.name LIKE %?1%")
	public Page<Category> search(String keyword, Pageable page);
	
	public Category findByName(String name);
	
	public Category findByAlias(String alias);
	
	public Long countById(Integer id);
	
	@Query("UPDATE Category c SET c.enabled = ?2 WHERE c.id = ?1")
	@Modifying
	public void updateEnabledStatus(Integer id, boolean enabled);
}
