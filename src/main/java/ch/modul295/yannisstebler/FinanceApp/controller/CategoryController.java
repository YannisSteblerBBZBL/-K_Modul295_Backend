package ch.modul295.yannisstebler.financeapp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.modul295.yannisstebler.financeapp.model.Category;
import ch.modul295.yannisstebler.financeapp.security.Roles;
import ch.modul295.yannisstebler.financeapp.services.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;

/**
 * Controller class for managing categories.
 * Provides endpoints for creating, updating, retrieving, and deleting categories.
 */
@RestController
@RequestMapping("/categories")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Endpoint to retrieve all categories.
     * Only users with the USER role can access this endpoint.
     *
     * @return A list of all categories.
     */
    @GetMapping
    @RolesAllowed(Roles.USER)
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Endpoint to retrieve a specific category by its ID.
     * Only users with the USER role can access this endpoint.
     *
     * @param id The ID of the category.
     * @return The requested category or HTTP status NOT_FOUND if the category does not exist.
     */
    @GetMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);
        return category.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Endpoint to create a new category.
     * Only users with the ADMIN role can create a category.
     *
     * @param category The category data to be created.
     * @return The created category.
     */
    @PostMapping
    @RolesAllowed(Roles.ADMIN)
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    /**
     * Endpoint to update an existing category by its ID.
     * Only users with the ADMIN role can update a category.
     *
     * @param id The ID of the category to be updated.
     * @param category The updated category data.
     * @return The updated category or HTTP status NOT_FOUND if the category does not exist.
     */
    @PutMapping("/{id}")
    @RolesAllowed(Roles.ADMIN)
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Optional<Category> existingCategory = categoryService.getCategoryById(id);

        if (existingCategory.isPresent()) {
            Category updatedCategory = categoryService.updateCategory(id, category);
            return ResponseEntity.ok(updatedCategory);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Endpoint to delete a category by its ID.
     * Only users with the ADMIN role can delete a category.
     *
     * @param id The ID of the category to be deleted.
     * @return HTTP status indicating the result of the deletion.
     */
    @DeleteMapping("/{id}")
    @RolesAllowed(Roles.ADMIN)
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);

        if (category.isPresent()) {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
