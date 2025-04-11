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

    // Helper method to extract username from JWT token
    private String getUsernameFromAuth(Authentication auth) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        return jwt.getClaim("preferred_username");
    }

    @GetMapping
    @RolesAllowed(Roles.USER)
    public ResponseEntity<List<Budget>> getAllBudgets(Authentication auth) {

        String username = getUsernameFromAuth(auth);

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            // Admin can access all budgets
            return ResponseEntity.ok(budgetService.getAllBudgets());
        }

        // Normal users can only access their own budgets
        List<Budget> userBudgets = budgetService.getAllBudgets().stream()
                .filter(budget -> budget.getKeycloak_username().equals(username))
                .toList();
        return ResponseEntity.ok(userBudgets);
    }

    @GetMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Budget> getBudgetById(Authentication auth, @PathVariable Long id) {

        String username = getUsernameFromAuth(auth);
        Optional<Budget> returnedBudget = budgetService.getBudgetById(id);

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            // Admin can access any budget
            return returnedBudget.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        }

        // Normal users can only access their own budget
        if (returnedBudget.isPresent() && returnedBudget.get().getKeycloak_username().equals(username)) {
            return ResponseEntity.ok(returnedBudget.get());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Budget> createBudget(Authentication auth, @RequestBody BudgetDTO budget) {
        String username = getUsernameFromAuth(auth);
        Budget createdBudget = budgetService.createBudget(username, budget);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBudget);
    }

    @PutMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Budget> updateBudget(Authentication auth, @PathVariable Long id, @RequestBody BudgetDTO budget) {

        String username = getUsernameFromAuth(auth);
        Optional<Budget> returnedBudget = budgetService.getBudgetById(id);

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            // Admin can update any budget
            Budget updatedBudget = budgetService.updateBudget(id, budget);
            return ResponseEntity.ok(updatedBudget);
        }

        // Normal users can only update their own budget
        if (returnedBudget.isPresent() && returnedBudget.get().getKeycloak_username().equals(username)) {
            Budget updatedBudget = budgetService.updateBudget(id, budget);
            return ResponseEntity.ok(updatedBudget);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @DeleteMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Void> deleteBudget(Authentication auth, @PathVariable Long id) {

        String username = getUsernameFromAuth(auth);
        Optional<Budget> returnedBudget = budgetService.getBudgetById(id);

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            // Admin can delete any budget
            budgetService.deleteBudget(id);
            return ResponseEntity.noContent().build();
        }

        // Normal users can only delete their own budget
        if (returnedBudget.isPresent() && returnedBudget.get().getKeycloak_username().equals(username)) {
            budgetService.deleteBudget(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
