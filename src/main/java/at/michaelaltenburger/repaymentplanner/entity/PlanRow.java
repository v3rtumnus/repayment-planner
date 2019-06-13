package at.michaelaltenburger.repaymentplanner.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PlanRow {
    private LocalDate date;
    private BigDecimal balanceChange;
    private BigDecimal newBalance;
    private DateType dateType;

    public PlanRow(LocalDate date, BigDecimal balanceChange, BigDecimal newBalance, DateType dateType) {
        this.date = date;
        this.balanceChange = balanceChange;
        this.newBalance = newBalance;
        this.dateType = dateType;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getBalanceChange() {
        return balanceChange;
    }

    public void setBalanceChange(BigDecimal balanceChange) {
        this.balanceChange = balanceChange;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public DateType getDateType() {
        return dateType;
    }

    public void setDateType(DateType dateType) {
        this.dateType = dateType;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        String formattedDate = date.format(formatter);

        return formattedDate + " || " + String.format("%1$10s", balanceChange) + " || " + String.format("%1$10s", newBalance) + " || " + dateType;
    }
}
