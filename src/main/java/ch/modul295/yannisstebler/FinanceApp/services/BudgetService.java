package ch.modul295.yannisstebler.financeapp.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import ch.modul295.yannisstebler.financeapp.model.Budget;
import ch.modul295.yannisstebler.financeapp.model.dto.BudgetDTO;
import ch.modul295.yannisstebler.financeapp.repository.BudgetRepository;
import ch.modul295.yannisstebler.financeapp.repository.CategoryRepository;

/**
 * Service class for handling business logic related to budgets.
 */
@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository; 

    /**
     * Helper method to get the 'preferred_username' from the JWT token.
     *
     * @return the 'preferred_username' from the JWT token.
     */
    private String getPreferredUsernameFromToken() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("preferred_username");
    }

    /**
     * Retrieves all budgets from the database.
     *
     * @return a list of all budgets.
     */
    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    /**
     * Retrieves a specific budget by its ID.
     *
     * @param id the ID of the budget.
     * @return an Optional containing the found budget or empty if not found.
     */
    public Optional<Budget> getBudgetById(Long id) {
        return budgetRepository.findById(id);
    }

    /**
     * Creates a new budget with the provided information.
     *
     * @param username the username of the user creating the budget.
     * @param budgetDTO the DTO containing budget creation data.
     * @return the newly created budget.
     */
    public Budget createBudget(String username, BudgetDTO budgetDTO) {
        Budget budget = new Budget();
        budget.setKeycloak_username(username); 
        budget.setCategory(categoryRepository.findById(budgetDTO.getCategory_id()).get()); 
        budget.setLimit_amount(budgetDTO.getLimit_amount()); 
        return budgetRepository.save(budget);
    }

    /**
     * Updates an existing budget with the provided information.
     *
     * @param id the ID of the budget to be updated.
     * @param budgetDTO the DTO containing updated budget data.
     * @return the updated budget.
     * @throws IllegalArgumentException if the budget with the given ID does not exist.
     */
    public Budget updateBudget(Long id, BudgetDTO budgetDTO) {
        if (budgetRepository.existsById(id)) { 

            String preferredUsername = getPreferredUsernameFromToken();

            Budget budget = budgetRepository.findById(id).get();
            budget.setKeycloak_username(preferredUsername); 
            budget.setCategory(categoryRepository.findById(budgetDTO.getCategory_id()).get());
            budget.setLimit_amount(budgetDTO.getLimit_amount()); 

            return budgetRepository.save(budget); 
        } else {
            throw new IllegalArgumentException("Budget not found"); 
        }
    }

    /**
     * Deletes a specific budget by its ID.
     *
     * @param id the ID of the budget to be deleted.
     * @return an Optional containing the deleted budget or empty if not found.
     */
    public Optional<Budget> deleteBudget(Long id) {
        Optional<Budget> deletedBudget = budgetRepository.findById(id); 
        budgetRepository.deleteById(id);
        return deletedBudget; 
    }
}
