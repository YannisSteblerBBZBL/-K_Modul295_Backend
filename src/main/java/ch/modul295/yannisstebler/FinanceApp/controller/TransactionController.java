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

import ch.modul295.yannisstebler.financeapp.model.Transaction;
import ch.modul295.yannisstebler.financeapp.model.dto.TransactionDTO;
import ch.modul295.yannisstebler.financeapp.security.Roles;
import ch.modul295.yannisstebler.financeapp.services.TransactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/transactions")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Helper method to extract username from JWT token
    private String getUsernameFromAuth(Authentication auth) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        return jwt.getClaim("preferred_username");
    }

    @GetMapping
    @RolesAllowed(Roles.USER)
    public List<Transaction> getAllTransactions(Authentication auth) {

        String username = getUsernameFromAuth(auth);

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(Roles.ADMIN))) {
            // Admin can access all transactions
            return transactionService.getAllTransactions();
        }

        // Normal users can only access their own transactions
        return transactionService.getAllTransactions().stream()
                .filter(transaction -> transaction.getKeycloak_username().equals(username))
                .toList();
    }

    @GetMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public Optional<Transaction> getTransactionById(Authentication auth, @PathVariable Long id) {

        String username = getUsernameFromAuth(auth);
        Optional<Transaction> returnedTransaction = transactionService.getTransactionById(id);

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(Roles.ADMIN))) {
            // Admin can access any transaction
            return returnedTransaction;
        }

        // Normal users can only access their own transaction
        if (returnedTransaction.isPresent() && returnedTransaction.get().getKeycloak_username().equals(username)) {
            return returnedTransaction;
        }

        return Optional.empty();
    }

    @PostMapping
    @RolesAllowed(Roles.USER)
    public Transaction createTransaction(Authentication auth, @RequestBody TransactionDTO transaction) {
        String username = getUsernameFromAuth(auth);
        return transactionService.createTransaction(username, transaction);
    }

    @PutMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public Transaction updateTransaction(Authentication auth, @PathVariable Long id, @RequestBody Transaction transaction) {
        String username = getUsernameFromAuth(auth);
        return transactionService.updateTransaction(username, id, transaction);
    }

    @DeleteMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public Optional<Transaction> deleteTransaction(Authentication auth, @PathVariable Long id) {
        String username = getUsernameFromAuth(auth);
        Optional<Transaction> returnedTransaction = transactionService.getTransactionById(id);

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(Roles.ADMIN))) {
            // Admin can delete any transaction
            return transactionService.deleteTransaction(id);
        }

        // Normal users can only delete their own transaction
        if (returnedTransaction.isPresent() && returnedTransaction.get().getKeycloak_username().equals(username)) {
            return transactionService.deleteTransaction(id);
        }

        return Optional.empty();
    }
}
