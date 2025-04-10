package ch.modul295.yannisstebler.FinanceApp.model;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "keycloak_username", nullable = false)
    String keycloak_username;

    @ManyToOne
    @JoinColumn(name = "category")
    Category category;

    @Column(nullable = false)
    BigDecimal amount;

    @Column(nullable = false, unique = true)
    Type type;

    @Column(nullable = false, unique = true)
    Date date;

    public enum Type {
        INCOME, EXPENSE
    }
}
