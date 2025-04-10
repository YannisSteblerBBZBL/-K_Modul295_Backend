package ch.modul295.yannisstebler.financeapp.model.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransactionDTO {

    Long category_id;

    BigDecimal amount;

    Type type;

    public enum Type {
        INCOME, EXPENSE
    }
    }
