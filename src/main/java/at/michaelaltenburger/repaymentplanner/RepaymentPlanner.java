package at.michaelaltenburger.repaymentplanner;

import at.michaelaltenburger.repaymentplanner.util.DateUtil;
import at.michaelaltenburger.repaymentplanner.util.Tuple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class RepaymentPlanner {

    private static final LocalDate START_DATE = LocalDate.of(2019, 1, 1);
    private static final BigDecimal START_AMOUNT = BigDecimal.valueOf(100_000);
    private static final BigDecimal INSTALLMENT_AMOUNT = BigDecimal.valueOf(1000);
    private static final BigDecimal INTEREST_RATE = BigDecimal.valueOf(1.5);
    private static final BigDecimal PROCESSING_FEE = BigDecimal.valueOf(16.9);
    private static final Map<LocalDate, BigDecimal> ADDITIONAL_PAYMENTS = new TreeMap<>();

    public static void main(String... args) {
        LocalDate currentDate = START_DATE;
        BigDecimal currentAmount = START_AMOUNT;

        Map<LocalDate, Tuple<DateType, BigDecimal>> balanceChanges = new TreeMap<>();
        balanceChanges.put(currentDate, new Tuple<>(DateType.INITIAL_AMOUNT, currentAmount));

        while (currentAmount.compareTo(BigDecimal.ZERO) > 1) {
            //get next balance change date
            Tuple<LocalDate, DateType> nextBalanceChanging = getNextBalanceChangingDate(currentDate);

            switch (nextBalanceChanging.y) {
                case INSTALLMENT:
                    balanceChanges.put(nextBalanceChanging.x, new Tuple<>(DateType.INSTALLMENT, INSTALLMENT_AMOUNT));
                    currentAmount = currentAmount.min(INSTALLMENT_AMOUNT);
                    break;
                case ADDITIONAL_PAYMENT:
                    BigDecimal paymentAmount = ADDITIONAL_PAYMENTS.get(nextBalanceChanging.x);

                    balanceChanges.put(nextBalanceChanging.x, new Tuple<>(DateType.ADDITIONAL_PAYMENT, paymentAmount));
                    currentAmount = currentAmount.min(paymentAmount);

                    ADDITIONAL_PAYMENTS.remove(nextBalanceChanging.x);
                    break;
                case END_OF_QUARTER:
                    break;
            }
        }
    }

    private static Tuple<LocalDate, DateType> getNextBalanceChangingDate(LocalDate currentDate) {
        Tuple<LocalDate, DateType> nextBalanceChangingDate =
                new Tuple<>(DateUtil.getNextInstallmentDate(currentDate), DateType.INSTALLMENT);

        LocalDate endOfQuarterDate = DateUtil.getEndOfQuarter(currentDate);

        if (endOfQuarterDate.isBefore(nextBalanceChangingDate.x)) {
            nextBalanceChangingDate = new Tuple<>(endOfQuarterDate, DateType.END_OF_QUARTER);
        }

        LocalDate nextAdditionalPaymentDate = ADDITIONAL_PAYMENTS.size() > 0 ?
                ADDITIONAL_PAYMENTS.entrySet().iterator().next().getKey() : LocalDate.MAX;

        return nextAdditionalPaymentDate.isBefore(nextBalanceChangingDate.x) ?
                new Tuple<>(nextAdditionalPaymentDate, DateType.ADDITIONAL_PAYMENT) : nextBalanceChangingDate;
    }
}
