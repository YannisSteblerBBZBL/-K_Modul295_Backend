package ch.modul295.yannisstebler.financeapp.model.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BudgetDTO {

    Long category_id;

    BigDecimal limit_amount;
}
