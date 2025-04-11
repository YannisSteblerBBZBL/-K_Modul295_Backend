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

/**
 * Controller class for managing transactions.
 * Provides endpoints for creating, updating, retrieving, and deleting transactions.
 */
@RestController
@RequestMapping("/transactions")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    /**
     * Helper method to extract the username from the JWT token.
     * 
     * @param auth The authentication object containing the JWT.
     * @return The username extracted from the JWT token.
     */
    private String getUsernameFromAuth(Authentication auth) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        return jwt.getClaim("preferred_username");
    }

    /**
     * Endpoint to retrieve all transactions.
     * Only users with the USER role can access this endpoint.
     * Admins can access all transactions, while normal users can only access their own transactions.
     *
     * @param auth The authentication object containing the user's details.
     * @return A list of transactions that the user can access.
     */
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

    /**
     * Endpoint to retrieve a specific transaction by its ID.
     * Only users with the USER role can access this endpoint.
     * Admins can access any transaction, while normal users can only access their own transactions.
     *
     * @param auth The authentication object containing the user's details.
     * @param id The ID of the transaction to be retrieved.
     * @return The requested transaction or HTTP status NOT_FOUND if the transaction does not exist.
     */
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

    /**
     * Endpoint to create a new transaction.
     * Only users with the USER role can create a transaction.
     *
     * @param auth The authentication object containing the user's details.
     * @param transactionDTO The transaction data to be created.
     * @return The created transaction.
     */
    @PostMapping
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Transaction> createTransaction(Authentication auth, @RequestBody TransactionDTO transactionDTO) {
        String username = getUsernameFromAuth(auth);
        Transaction createdTransaction = transactionService.createTransaction(username, transactionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    /**
     * Endpoint to update an existing transaction by its ID.
     * Only users with the USER role can update their own transactions.
     * Admins can update any transaction.
     *
     * @param auth The authentication object containing the user's details.
     * @param id The ID of the transaction to be updated.
     * @param transaction The updated transaction data.
     * @return The updated transaction or HTTP status FORBIDDEN if the user doesn't own the transaction.
     */
    @PutMapping("/{id}")
    @RolesAllowed(Roles.USER)
    public ResponseEntity<Transaction> updateTransaction(Authentication auth, @PathVariable Long id, @RequestBody Transaction transaction) {
        String username = getUsernameFromAuth(auth);

        // Check if the transaction belongs to the user or if user is an admin
        if (auth.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + Roles.ADMIN))
                || transaction.getKeycloak_username().equals(username)) {
            Transaction updatedTransaction = transactionService.updateTransaction(username, id, transaction);
            return ResponseEntity.ok(updatedTransaction);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();  // Forbidden if the user doesn't own the transaction
    }

    /**
     * Endpoint to delete a transaction by its ID.
     * Only users with the USER role can delete their own transactions.
     * Admins can delete any transaction.
     *
     * @param auth The authentication object containing the user's details.
     * @param id The ID of the transaction to be deleted.
     * @return HTTP status indicating the result of the deletion.
     */
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
