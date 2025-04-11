package ch.modul295.yannisstebler.financeapp.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.modul295.yannisstebler.financeapp.model.Category;
import ch.modul295.yannisstebler.financeapp.repository.CategoryRepository;

/**
 * Service class for handling business logic related to categories.
 */
@Service
public class CategoryService {

    @Autowired
    private final CategoryRepository categoryRepository; 

    /**
     * Constructor for CategoryService.
     *
     * @param categoryRepository the category repository to be injected.
     */
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Retrieves all categories from the database.
     *
     * @return a list of all categories.
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll(); 
    }

    /**
     * Retrieves a specific category by its ID.
     *
     * @param id the ID of the category.
     * @return an Optional containing the found category or empty if not found.
     */
    public Optional<Category> getCategoryById(Long id) {
        Optional<Category> category = categoryRepository.findById(id); 
        return category;
    }

    /**
     * Creates a new category in the database.
     *
     * @param category the category to be created.
     * @return the newly created category.
     */
    public Category createCategory(Category category) {
        return categoryRepository.save(category); 
    }

    /**
     * Updates an existing category.
     *
     * @param id the ID of the category to be updated.
     * @param category the updated category data.
     * @return the updated category.
     * @throws IllegalArgumentException if the category with the given ID does not exist.
     */
    public Category updateCategory(Long id, Category category) {
        if (categoryRepository.existsById(id)) {  
            category.setId(id); 
            Category updatedCategory = categoryRepository.save(category);
            return updatedCategory;
        } else {
            throw new IllegalArgumentException("Category not found"); 
        }
    }

    /**
     * Deletes a specific category by its ID.
     *
     * @param id the ID of the category to be deleted.
     * @return an Optional containing the deleted category or empty if not found.
     */
    public Optional<Category> deleteCategory(Long id) {
        Optional<Category> deletedCategory = categoryRepository.findById(id);
        categoryRepository.deleteById(id);
        return deletedCategory; 
    }
}
