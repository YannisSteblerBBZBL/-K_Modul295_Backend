package ch.modul295.yannisstebler.FinanceApp.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.modul295.yannisstebler.FinanceApp.model.Category;
import ch.modul295.yannisstebler.FinanceApp.repository.CategoryRepository;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category;
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category category) {
        if (categoryRepository.existsById(id)) {
            category.setId(id);
            Category updatedCategory = categoryRepository.save(category);
            return updatedCategory;
        } else {
            throw new IllegalArgumentException("Category not found");
        }
    }

    public Optional<Category> deleteCategory(Long id) {
        Optional<Category> deletedCategory = categoryRepository.findById(id);
        categoryRepository.deleteById(id);
        return deletedCategory;
    }
}
