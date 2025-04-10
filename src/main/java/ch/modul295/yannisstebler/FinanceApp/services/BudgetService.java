package ch.modul295.yannisstebler.FinanceApp.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import ch.modul295.yannisstebler.FinanceApp.model.Budget;
import ch.modul295.yannisstebler.FinanceApp.model.dto.BudgetDTO;
import ch.modul295.yannisstebler.FinanceApp.repository.BudgetRepository;
import ch.modul295.yannisstebler.FinanceApp.repository.CategoryRepository;
@Service
public class BudgetService {
    
    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private String getPreferredUsernameFromToken() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("preferred_username");
    }

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public Optional<Budget> getBudgetById(Long id) {
        Optional<Budget> budget = budgetRepository.findById(id);
        return budget;
    }

    public Budget createBudget(String username, BudgetDTO budgetDTO) {
        Budget budget = new Budget();
        budget.setKeycloak_username(username);
        budget.setCategory(categoryRepository.findById(budgetDTO.getCategory_id()).get());
        budget.setLimit_amount(budget.getLimit_amount());
        return budgetRepository.save(budget);
    }

    public Budget updateBudget(Long id, Budget budget) {
        if (budgetRepository.existsById(id)) {
            String preferredUsername = getPreferredUsernameFromToken();
            budget.setKeycloak_username(preferredUsername);
            budget.setId(id);
            Budget updatedBudget = budgetRepository.save(budget);
            return updatedBudget;
        } else {
            throw new IllegalArgumentException("Budget not found");
        }
    }

    public Optional<Budget> deleteBudget(Long id) {
        Optional<Budget> deletedBudget = budgetRepository.findById(id);
        budgetRepository.deleteById(id);
        return deletedBudget;
    }
}
