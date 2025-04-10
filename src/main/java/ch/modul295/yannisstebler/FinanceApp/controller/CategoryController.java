package ch.modul295.yannisstebler.FinanceApp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.modul295.yannisstebler.FinanceApp.model.Category;
import ch.modul295.yannisstebler.FinanceApp.security.Roles;
import ch.modul295.yannisstebler.FinanceApp.services.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/categories")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @RolesAllowed(Roles.User)
    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    @RolesAllowed(Roles.User)
    public Optional<Category> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping
    @RolesAllowed(Roles.User)
    public Category createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

    @PutMapping("/{id}")
    @RolesAllowed(Roles.User)
    public Category updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.updateCategory(id, category);
    }

    @DeleteMapping("/{id}")
    @RolesAllowed(Roles.User)
    public Optional<Category> deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
}
