package ch.modul295.yannisstebler.financeapp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping("/api/categories")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Helper method to extract username from JWT token.
     *
     * @param auth The authentication object containing the JWT token.
     * @return The username from the JWT token.
     */
    private String getUsernameFromAuth(Authentication auth) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        return jwt.getClaim("preferred_username");
    }

    /**
     * Endpoint to retrieve all categories.
     * Only users with the USER role can access this endpoint.
     * Admins can access all categories, normal users only their own.
     *
     * @param auth The authentication object containing the JWT token.
     * @return A list of categories.
     */
    @GetMapping
    @RolesAllowed(Roles.USER)
    public ResponseEntity<List<Category>> getAllCategories(Authentication auth) {
        String username = getUsernameFromAuth(auth);

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            // Admin can access all categories
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        }

        // Normal users can only access their own categories
        List<Category> userCategories = categoryService.getAllCategories().stream()
                .filter(category -> category.getKeycloak_username().equals(username))
                .toList();

        return ResponseEntity.ok(userCategories);
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
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id, Authentication auth) {
        String username = getUsernameFromAuth(auth);
        Optional<Category> category = categoryService.getCategoryById(id);

        if (category.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Admins can access any category
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            return ResponseEntity.ok(category.get());
        }

        // Normal users can only access their own categories
        if (category.get().getKeycloak_username().equals(username)) {
            return ResponseEntity.ok(category.get());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Endpoint to create a new category.
     * Only users with the ADMIN role can create a category.
     *
     * @param category The category data to be created.
     * @return The created category.
     */
    @PostMapping
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Category> createCategory(Authentication auth, @RequestBody Category category) {
        String username = getUsernameFromAuth(auth);
        category.setKeycloak_username(username);
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
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Category> updateCategory(Authentication auth, @PathVariable Long id, @RequestBody Category category) {
        Optional<Category> existingCategory = categoryService.getCategoryById(id);
        String username = getUsernameFromAuth(auth);
        category.setKeycloak_username(username);
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
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Optional<Category> category = categoryService.getCategoryById(id);

        if (category.isPresent()) {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
