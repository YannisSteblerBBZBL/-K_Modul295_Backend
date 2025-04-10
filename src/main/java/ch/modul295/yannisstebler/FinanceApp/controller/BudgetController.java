package ch.modul295.yannisstebler.financeapp.controller;

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

import ch.modul295.yannisstebler.financeapp.model.Budget;
import ch.modul295.yannisstebler.financeapp.model.dto.BudgetDTO;
import ch.modul295.yannisstebler.financeapp.security.Roles;
import ch.modul295.yannisstebler.financeapp.services.BudgetService;
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
    @RolesAllowed(Roles.USER)
    public List<Budget> getAllBudgets(Authentication auth) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        String username = jwt.getClaim("preferred_username");

        List<Budget> returnedBudgets = budgetService.getAllBudgets().stream()
                .filter(budget -> budget.getKeycloak_username().equals(username))
                .toList();

        return returnedBudgets;
    }

    @GetMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public Optional<Budget> getBudgetById(Authentication auth, @PathVariable Long id) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        String username = jwt.getClaim("preferred_username");

        Optional<Budget> returnedBudget = budgetService.getBudgetById(id);
        if (returnedBudget.isPresent()) {
            Budget budget = returnedBudget.get();
            if (!budget.getKeycloak_username().equals(username)) {
                return Optional.empty();
            }
        }

        return returnedBudget;
    }

    @PostMapping
    @RolesAllowed(Roles.USER)
    public Budget createBudget(Authentication auth, @RequestBody BudgetDTO budget) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        String username = jwt.getClaim("preferred_username");
        return budgetService.createBudget(username, budget);
    }

    @PutMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public Budget updateBudget(Authentication auth, @PathVariable Long id, @RequestBody BudgetDTO budget) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        String username = jwt.getClaim("preferred_username");
        Optional<Budget> returnedBudget = budgetService.getBudgetById(id);
        if (!returnedBudget.get().getKeycloak_username().equals(username)) {
            return null;
        }
        return budgetService.updateBudget(id, budget);
    }

    @DeleteMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public Optional<Budget> deleteBudget(Authentication auth, @PathVariable Long id) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        String username = jwt.getClaim("preferred_username");
        Optional<Budget> returnedBudget = budgetService.getBudgetById(id);
        if (!returnedBudget.get().getKeycloak_username().equals(username)) {
            return null;
        }
        return budgetService.deleteBudget(id);
    }
}
