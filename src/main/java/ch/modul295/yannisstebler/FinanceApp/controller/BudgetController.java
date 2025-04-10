package ch.modul295.yannisstebler.FinanceApp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import ch.modul295.yannisstebler.FinanceApp.model.Budget;
import ch.modul295.yannisstebler.FinanceApp.model.dto.BudgetDTO;
import ch.modul295.yannisstebler.FinanceApp.security.Roles;
import ch.modul295.yannisstebler.FinanceApp.services.BudgetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/budgets")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class BudgetController {
    
    @Autowired
    private BudgetService budgetService;

    @GetMapping
    @RolesAllowed(Roles.User)
    public List<Budget> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    @GetMapping("/{id}")
    @RolesAllowed(Roles.User)
    public Optional<Budget> getBudgetById(@PathVariable Long id) {
        return budgetService.getBudgetById(id);
    }

    @PostMapping
    @RolesAllowed(Roles.User)
    public Budget createBudget(Authentication auth, @RequestBody BudgetDTO budget) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        String username = jwt.getClaim("preferred_username");
        return budgetService.createBudget(username, budget);
    }

    @PutMapping("/{id}")
    @RolesAllowed(Roles.User)
    public Budget updateBudget(@PathVariable Long id, @RequestBody Budget budget) {
        return budgetService.updateBudget(id, budget);
    }

    @DeleteMapping("/{id}")
    @RolesAllowed(Roles.User)
    public Optional<Budget> deleteBudget(@PathVariable Long id) {
        return budgetService.deleteBudget(id);
    }
}
