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

import ch.modul295.yannisstebler.FinanceApp.model.Transaction;
import ch.modul295.yannisstebler.FinanceApp.model.dto.TransactionDTO;
import ch.modul295.yannisstebler.FinanceApp.security.Roles;
import ch.modul295.yannisstebler.FinanceApp.services.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/transactions")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;

    @GetMapping
    @RolesAllowed(Roles.User)
    public List<Transaction> getAllTransaction() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    @RolesAllowed(Roles.User)
    public Optional<Transaction> getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    @PostMapping
    @RolesAllowed(Roles.User)
    public Transaction createTransaction(Authentication auth, @RequestBody TransactionDTO transaction) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        String username = jwt.getClaim("preferred_username");
        return transactionService.createTransaction(username, transaction);
    }

    @PutMapping("/{id}")
    @RolesAllowed(Roles.User)
    public Transaction updateTransaction(Authentication auth, @PathVariable Long id, @RequestBody Transaction transaction) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        String username = jwt.getClaim("preferred_username");
        return transactionService.updateTransaction(username, id, transaction);
    }

    @DeleteMapping("/{id}")
    @RolesAllowed(Roles.User)
    public Optional<Transaction> deleteTransaction(@PathVariable Long id) {
        return transactionService.deleteTransaction(id);
    }
}
