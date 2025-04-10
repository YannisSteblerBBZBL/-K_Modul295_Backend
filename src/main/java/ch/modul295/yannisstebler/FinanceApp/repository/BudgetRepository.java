package ch.modul295.yannisstebler.financeapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.modul295.yannisstebler.financeapp.model.Budget;


@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
}
