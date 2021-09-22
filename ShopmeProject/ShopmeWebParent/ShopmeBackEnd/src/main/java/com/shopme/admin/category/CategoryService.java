package com.shopme.admin.category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopme.common.entity.Category;
import com.shopme.common.exception.CategoryNotFoundException;

@Service
@Transactional
public class CategoryService {
	
	public static final int ROOT_CATEGORIES_PER_PAGE = 4;
	
	@Autowired
	private CategoryRepository repoCategory;
	
	public List<Category> listByPage(CategoryPageInfo pageInfo, int pageNum, String sortDir, String keyword) {
		Sort sort = Sort.by("name");
		
		if (sortDir.equals("asc")) {
			sort = sort.ascending();
		} else if (sortDir.equals("desc")) {
			sort = sort.descending();
		}
		
		Pageable pageable = PageRequest.of(pageNum-1, ROOT_CATEGORIES_PER_PAGE, sort);
		Page<Category> pageCategories = null;
		
		if (keyword != null && !keyword.isEmpty()) {
			pageCategories = repoCategory.search(keyword, pageable);
		} else {
			pageCategories = repoCategory.findRootCategories(pageable);
		}
			
		List<Category> rootCategories = pageCategories.getContent();
		
		pageInfo.setTotalPages(pageCategories.getTotalPages());
		pageInfo.setTotalElements(pageCategories.getTotalElements());
		
		if (keyword != null && !keyword.isEmpty()) {
			List<Category> searchResult = pageCategories.getContent();
			
			for (Category category : searchResult) {
				category.setHasChildren(category.getChildren().size() > 0);
			}
			return searchResult;
		} else {
			return listHierarchicalCategories(rootCategories, sortDir);
		}
	}
	
	public List<Category> listHierarchicalCategories(List<Category> rootCategories, String sortDir) {
		List<Category> listHierarchicalCategories = new ArrayList<>();
		
		for (Category rootCategory : rootCategories) {
			listHierarchicalCategories.add(Category.copyFull(rootCategory));
			
			Set<Category> children = sortSubCategories(rootCategory.getChildren(), sortDir);
			
			for (Category subCategory : children) {
				String name = "--" + subCategory.getName();
				listHierarchicalCategories.add(Category.copyFull(subCategory, name));
				
				listSubHierarchicalCategories(listHierarchicalCategories, 1, subCategory, sortDir);
			}
		}
		
		return listHierarchicalCategories;
	}
	
	public void delete(Integer id) throws CategoryNotFoundException {
		Long countById = repoCategory.countById(id);
		
		if (countById == null || countById == 0) {
			throw new CategoryNotFoundException("Could not find any category with ID " + id);
		}
		
		repoCategory.deleteById(id);
	}
	
	private void listSubHierarchicalCategories(List<Category> listHierarchicalCategories, int subLevel, 
												Category parent, String sortDir) {
		Set<Category> children = sortSubCategories(parent.getChildren(), sortDir);
		int newSubLevel = subLevel + 1;
		
		for (Category subCategory : children) {
			String name = "";
			for (int i = 0; i < newSubLevel; i++) {
				name += "--";
			}
			name += subCategory.getName();
			listHierarchicalCategories.add(Category.copyFull(subCategory, name));
			
			listSubHierarchicalCategories(listHierarchicalCategories, newSubLevel, subCategory, sortDir);
		}
	}
	
	public List<Category> listCategoriesUsedInForm() {
		
		List<Category> categoriesUsedInForm = new ArrayList<Category>();
		Iterable<Category> categoriesInDB = repoCategory.findRootCategories(Sort.by("name").ascending());
		
		for (Category category : categoriesInDB) {
			if (category.getParent() == null) {
				categoriesUsedInForm.add(Category.copyIdAndName(category));
				
				Set<Category> children = sortSubCategories(category.getChildren());
				
				for (Category subCategory : children) {
					String name = "--" + subCategory.getName();
					categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
					listSubCategoriesUsedInForm(categoriesUsedInForm, subCategory, 1);
				}
			}
		}
		
		return categoriesUsedInForm;
	}
	
	private SortedSet<Category> sortSubCategories(Set<Category> children) {
		return sortSubCategories(children, "asc");
	}
	
	private SortedSet<Category> sortSubCategories(Set<Category> children, String sortDir) {
		SortedSet<Category> sortedChildren = new TreeSet<>(new Comparator<Category>() {

			@Override
			public int compare(Category cat1, Category cat2) {
				if (sortDir.equals("asc")) {
					return cat1.getName().compareTo(cat2.getName());
				} else {
					return cat2.getName().compareTo(cat1.getName());
				}
				
			}
		});
		
		sortedChildren.addAll(children);
		return sortedChildren;
	}
	
	private void listSubCategoriesUsedInForm(List<Category> categoriesUsedInForm, Category parent, int subLevel) {
		
		int newSubLevel = subLevel + 1;
		Set<Category> children = sortSubCategories(parent.getChildren());
		
		for (Category subCategory : children) {
			String name = "";
			for (int i = 0; i < newSubLevel; i++) {
				name += "--";
			}
			name += subCategory.getName();
			categoriesUsedInForm.add(Category.copyIdAndName(subCategory.getId(), name));
			
			listSubCategoriesUsedInForm(categoriesUsedInForm, subCategory, newSubLevel);
		}
	}
	
	public String checkUnique(Integer id, String name, String alias) {
		boolean isCreatingNew = (id == null || id == 0);
		Category categoryByName = repoCategory.findByName(name);
		
		if (isCreatingNew) {
			if (categoryByName != null) {
				return "DuplicateName";
			} else {
				Category categoryByAlias = repoCategory.findByAlias(alias);
				if (categoryByAlias != null) {
					return "DuplicateAlias";
				}
			}
		} else {
			if (categoryByName != null && categoryByName.getId() != id) {
				return "DuplicateName";
			}
			
			Category categoryByAlias = repoCategory.findByAlias(alias);
			
			if (categoryByAlias != null && categoryByAlias.getId() != id) {
				return "DuplicateAlias";
			}
		}
		
		return "OK";
	}
	
	public Category get(Integer id) throws CategoryNotFoundException {
		try {
			return repoCategory.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new CategoryNotFoundException("Cound not find any category with ID " + id);
		}
	}
	
	public Category save(Category category) {

		Category parent = category.getParent();
		if (parent != null) {
			String allParentIds = parent.getAllParentIDs() == null ? "-" : parent.getAllParentIDs();
			allParentIds += String.valueOf(parent.getId()) + "-";
			category.setAllParentIDs(allParentIds);
		}

		return repoCategory.save(category);
	}
	
	public void updateCategoryEnabledStatus(Integer id, boolean enabled) {
		repoCategory.updateEnabledStatus(id, enabled);
	}

}
