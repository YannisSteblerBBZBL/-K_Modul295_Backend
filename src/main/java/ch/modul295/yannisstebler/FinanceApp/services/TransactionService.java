package ch.modul295.yannisstebler.financeapp.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.modul295.yannisstebler.financeapp.model.Transaction;
import ch.modul295.yannisstebler.financeapp.model.dto.TransactionDTO;
import ch.modul295.yannisstebler.financeapp.repository.CategoryRepository;
import ch.modul295.yannisstebler.financeapp.repository.TransactionRepository;

/**
 * Service class for managing transactions.
 */
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Retrieves all transactions from the repository.
     * 
     * @return a list of all transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Retrieves a specific transaction by its ID.
     * 
     * @param id the ID of the transaction to retrieve
     * @return an Optional containing the transaction if found, otherwise empty
     */
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id); 
    }

    /**
     * Creates a new transaction from the provided data transfer object (DTO).
     * 
     * @param username the username of the user creating the transaction
     * @param transactionDTO the data transfer object containing transaction details
     * @return the created transaction
     */
    public Transaction createTransaction(String username, TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();
        transaction.setKeycloak_username(username); 
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(Transaction.Type.valueOf(transactionDTO.getType().name())); 
        transaction.setDate(new Date());
        transaction.setCategory(categoryRepository.findById(transactionDTO.getCategory_id()).get());  
        return transactionRepository.save(transaction);
    }

    /**
     * Updates an existing transaction with new details.
     * 
     * @param username the username of the user updating the transaction
     * @param id the ID of the transaction to update
     * @param transaction the transaction object containing the updated details
     * @return the updated transaction
     * @throws IllegalArgumentException if the transaction is not found
     */
    public Transaction updateTransaction(String username, Long id, Transaction transaction) {
        if (transactionRepository.existsById(id)) {  
            transaction.setKeycloak_username(username);  
            transaction.setId(id);  
            return transactionRepository.save(transaction); 
        } else {
            throw new IllegalArgumentException("Transaction not found");  
        }
    }

    /**
     * Deletes a transaction by its ID.
     * 
     * @param id the ID of the transaction to delete
     * @return an Optional containing the deleted transaction, or empty if not found
     */
    public Optional<Transaction> deleteTransaction(Long id) {
        Optional<Transaction> deletedTransaction = transactionRepository.findById(id); 
        transactionRepository.deleteById(id); 
        return deletedTransaction;  
    }
}
