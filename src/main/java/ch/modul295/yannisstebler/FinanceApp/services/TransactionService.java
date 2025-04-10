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

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long id) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        return transaction;
    }

    public Transaction createTransaction(String username, TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();
        transaction.setKeycloak_username(username);
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(Transaction.Type.valueOf(transactionDTO.getType().name()));
        transaction.setDate(new Date());
        transaction.setCategory(categoryRepository.findById((transactionDTO.getCategory_id())).get());
        return transactionRepository.save(transaction);
    }

    public Transaction updateTransaction(String username, Long id, Transaction transaction) {
        if (transactionRepository.existsById(id)) {
            transaction.setKeycloak_username(username);
            transaction.setId(id);
            Transaction updatedTransaction = transactionRepository.save(transaction);
            return updatedTransaction;
        } else {
            throw new IllegalArgumentException("Transaction not found");
        }
    }

    public Optional<Transaction> deleteTransaction(Long id) {
        Optional<Transaction> deletedTransaction = transactionRepository.findById(id);
        transactionRepository.deleteById(id);
        return deletedTransaction;
    }
}
