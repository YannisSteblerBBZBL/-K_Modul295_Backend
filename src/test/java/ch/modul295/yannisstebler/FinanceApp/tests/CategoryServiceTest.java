package ch.modul295.yannisstebler.FinanceApp.tests;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.modul295.yannisstebler.financeapp.model.Category;
import ch.modul295.yannisstebler.financeapp.repository.CategoryRepository;
import ch.modul295.yannisstebler.financeapp.services.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryServiceTest {

    private CategoryService categoryService;
    private final CategoryRepository categoryRepositoryMock = mock(CategoryRepository.class);

    private final Category categoryMock = mock(Category.class);

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepositoryMock);
    }

    @Test
    void createCategory() {
        when(categoryRepositoryMock.save(categoryMock)).thenReturn(categoryMock);
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Description");
        categoryService.createCategory(category);
        verify(categoryRepositoryMock, times(1)).save(any());
    }

    @Test
    void findCategory() {
        when(categoryRepositoryMock.findById(any())).thenReturn(Optional.ofNullable(categoryMock));
        Optional<Category> c = categoryService.getCategoryById(any());
        assertTrue(c.isPresent());
        verify(categoryRepositoryMock, times(1)).findById(any());
    }

    @Test
    void deleteCategory() {
        categoryService.deleteCategory(any());
        verify(categoryRepositoryMock, times(1)).deleteById(any());
    }

    @Test
    void updateCategory() {
        when(categoryRepositoryMock.existsById(any())).thenReturn(true);
        when(categoryRepositoryMock.save(categoryMock)).thenReturn(categoryMock);

        Category updatedCategory = categoryService.updateCategory(any(), categoryMock);

        assertNotNull(updatedCategory);
        verify(categoryRepositoryMock, times(1)).existsById(any());
        verify(categoryRepositoryMock, times(1)).save(categoryMock);
    }

    @Test
    void findCategoryNotFound() {
        when(categoryRepositoryMock.findById(any())).thenReturn(Optional.empty());

        Optional<Category> c = categoryService.getCategoryById(any());

        assertFalse(c.isPresent());
        verify(categoryRepositoryMock, times(1)).findById(any());
    }

    @Test
    void deleteCategoryNotFound() {
        doThrow(new IllegalArgumentException("Category not found")).when(categoryRepositoryMock).deleteById(any());

        assertThrows(IllegalArgumentException.class, () -> categoryService.deleteCategory(any()));
        verify(categoryRepositoryMock, times(1)).deleteById(any());
    }
}
