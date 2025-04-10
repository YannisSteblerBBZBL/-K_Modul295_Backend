package ch.modul295.yannisstebler.FinanceApp.model.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BudgetDTO {

    Long category_id;

    BigDecimal limit_amount;
}
