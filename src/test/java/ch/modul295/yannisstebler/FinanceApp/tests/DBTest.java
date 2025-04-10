package ch.modul295.yannisstebler.FinanceApp.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import ch.modul295.yannisstebler.financeapp.model.Category;
import ch.modul295.yannisstebler.financeapp.repository.CategoryRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
public class DBTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void insertCategory() {
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("This is a test category");

        category = this.categoryRepository.save(category);
        Assertions.assertNotNull(category.getId());
    }
}
