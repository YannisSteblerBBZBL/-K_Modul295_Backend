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
    public ResponseEntity<List<Transaction>> getAllTransactions(Authentication auth) {

        String username = getUsernameFromAuth(auth);

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            // Admin can access all transactions
            List<Transaction> transactions = transactionService.getAllTransactions();
            return ResponseEntity.ok(transactions);
        }

        // Normal users can only access their own transactions
        List<Transaction> userTransactions = transactionService.getAllTransactions().stream()
                .filter(transaction -> transaction.getKeycloak_username().equals(username))
                .toList();
        
        return ResponseEntity.ok(userTransactions);
    }

    @GetMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Transaction> getTransactionById(Authentication auth, @PathVariable Long id) {

        String username = getUsernameFromAuth(auth);
        Optional<Transaction> returnedTransaction = transactionService.getTransactionById(id);

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            // Admin can access any transaction
            return returnedTransaction.map(ResponseEntity::ok)
                                      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

        // Normal users can only access their own transaction
        if (returnedTransaction.isPresent() && returnedTransaction.get().getKeycloak_username().equals(username)) {
            return ResponseEntity.ok(returnedTransaction.get());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  // Forbidden if the user doesn't own the transaction
    }

    @PostMapping
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Transaction> createTransaction(Authentication auth, @RequestBody TransactionDTO transactionDTO) {
        String username = getUsernameFromAuth(auth);
        Transaction createdTransaction = transactionService.createTransaction(username, transactionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    @PutMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Transaction> updateTransaction(Authentication auth, @PathVariable Long id, @RequestBody Transaction transaction) {
        String username = getUsernameFromAuth(auth);

        // Check if the transaction belongs to the user or if user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN)) || 
            transaction.getKeycloak_username().equals(username)) {
            Transaction updatedTransaction = transactionService.updateTransaction(username, id, transaction);
            return ResponseEntity.ok(updatedTransaction);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  // Forbidden if the user doesn't own the transaction
    }

    @DeleteMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Void> deleteTransaction(Authentication auth, @PathVariable Long id) {
        String username = getUsernameFromAuth(auth);
        Optional<Transaction> returnedTransaction = transactionService.getTransactionById(id);

        // Check if the user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))) {
            // Admin can delete any transaction
            transactionService.deleteTransaction(id);
            return ResponseEntity.noContent().build();  // 204 No Content
        }

        // Normal users can only delete their own transaction
        if (returnedTransaction.isPresent() && returnedTransaction.get().getKeycloak_username().equals(username)) {
            transactionService.deleteTransaction(id);
            return ResponseEntity.noContent().build();  // 204 No Content
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  // Forbidden if the user doesn't own the transaction
    }
}
