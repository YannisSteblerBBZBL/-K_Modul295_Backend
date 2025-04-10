package ch.modul295.yannisstebler.FinanceApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.modul295.yannisstebler.FinanceApp.model.Transaction;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
}
