package ch.modul295.yannisstebler.FinanceApp.model.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class TransactionDTO {

    Long category_id;

    BigDecimal amount;

    Type type;

    Date date;

    public enum Type {
        INCOME, EXPENSE
    }
    }
