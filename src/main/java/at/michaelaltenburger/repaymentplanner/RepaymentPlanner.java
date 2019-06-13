package at.michaelaltenburger.repaymentplanner;

import at.michaelaltenburger.repaymentplanner.entity.DateType;
import at.michaelaltenburger.repaymentplanner.entity.PlanRow;
import at.michaelaltenburger.repaymentplanner.util.DateUtil;
import at.michaelaltenburger.repaymentplanner.util.Tuple;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static java.time.temporal.ChronoUnit.DAYS;

public class RepaymentPlanner {

    private static final LocalDate START_DATE = LocalDate.of(2019, 5, 1);
    private static final BigDecimal START_AMOUNT = BigDecimal.valueOf(305_000);
    private static final BigDecimal INSTALLMENT_AMOUNT = BigDecimal.valueOf(960);
    private static final Double INTEREST_RATE = 1.5;
    private static final BigDecimal PROCESSING_FEE = BigDecimal.valueOf(16.9);
    private static final Map<LocalDate, BigDecimal> ADDITIONAL_PAYMENTS = new TreeMap<>();

    public static void main(String... args) {
        LocalDate currentDate = START_DATE;
        BigDecimal currentAmount = START_AMOUNT;
        int lastRowWithInterestCalculated = -1;

        List<PlanRow> planRows = new ArrayList<>();
        planRows.add(new PlanRow(currentDate, BigDecimal.ZERO, currentAmount, DateType.INITIAL_AMOUNT));

        while (currentAmount.compareTo(BigDecimal.ZERO) > 0) {
            //get next balance change date
            Tuple<LocalDate, DateType> nextBalanceChanging = getNextBalanceChangingDate(currentDate);

            switch (nextBalanceChanging.y) {
                case INSTALLMENT:
                    currentAmount = currentAmount.subtract(INSTALLMENT_AMOUNT);
                    planRows.add(new PlanRow(nextBalanceChanging.x, INSTALLMENT_AMOUNT, currentAmount, DateType.INSTALLMENT));
                    break;
                case ADDITIONAL_PAYMENT:
                    BigDecimal paymentAmount = ADDITIONAL_PAYMENTS.get(nextBalanceChanging.x);

                    currentAmount = currentAmount.subtract(paymentAmount);
                    planRows.add(new PlanRow(nextBalanceChanging.x, paymentAmount, currentAmount, DateType.ADDITIONAL_PAYMENT));

                    ADDITIONAL_PAYMENTS.remove(nextBalanceChanging.x);
                    break;
                case END_OF_QUARTER:
                    BigDecimal interestForQuarter = BigDecimal.ZERO;

                    ListIterator<PlanRow> planRowIterator = planRows.listIterator(lastRowWithInterestCalculated + 1);

                    PlanRow currentPlanRow = planRowIterator.next();

                    while (planRowIterator.hasNext()) {
                        PlanRow nextRow = planRowIterator.next();

                        long daysBetween = DAYS.between(currentPlanRow.getDate(), nextRow.getDate());

                        BigDecimal interestForStep = BigDecimal.valueOf(
                                currentPlanRow.getNewBalance().doubleValue() * daysBetween / 360.0 * INTEREST_RATE / 100)
                                .setScale(2, RoundingMode.HALF_UP);

                        interestForQuarter = interestForQuarter.add(interestForStep);

                        currentPlanRow = nextRow;
                    }

                    long daysBetween = DAYS.between(currentPlanRow.getDate(), nextBalanceChanging.x);

                    BigDecimal interestForStep = BigDecimal.valueOf(
                            currentPlanRow.getNewBalance().doubleValue() * daysBetween / 360.0 * INTEREST_RATE / 100)
                            .setScale(2, RoundingMode.HALF_UP);

                    interestForQuarter = interestForQuarter.add(interestForStep);

                    currentAmount = currentAmount.add(interestForQuarter.add(PROCESSING_FEE));

                    planRows.add(new PlanRow(nextBalanceChanging.x, interestForQuarter.add(PROCESSING_FEE), currentAmount, DateType.END_OF_QUARTER));

                    lastRowWithInterestCalculated = planRows.size() -2;

                    break;
            }

            currentDate = nextBalanceChanging.x;
        }

        for (PlanRow row : planRows) {
            System.out.println(row);
        }
    }

    private static Tuple<LocalDate, DateType> getNextBalanceChangingDate(LocalDate currentDate) {
        Tuple<LocalDate, DateType> nextBalanceChangingDate =
                new Tuple<>(DateUtil.getNextInstallmentDate(currentDate), DateType.INSTALLMENT);

        LocalDate endOfQuarterDate = DateUtil.getEndOfQuarter(currentDate);

        if (endOfQuarterDate.isBefore(nextBalanceChangingDate.x) && endOfQuarterDate.isAfter(currentDate)) {
            nextBalanceChangingDate = new Tuple<>(endOfQuarterDate, DateType.END_OF_QUARTER);
        }

        LocalDate nextAdditionalPaymentDate = ADDITIONAL_PAYMENTS.size() > 0 ?
                ADDITIONAL_PAYMENTS.entrySet().iterator().next().getKey() : LocalDate.MAX;

        return nextAdditionalPaymentDate.isBefore(nextBalanceChangingDate.x) ?
                new Tuple<>(nextAdditionalPaymentDate, DateType.ADDITIONAL_PAYMENT) : nextBalanceChangingDate;
    }
}
