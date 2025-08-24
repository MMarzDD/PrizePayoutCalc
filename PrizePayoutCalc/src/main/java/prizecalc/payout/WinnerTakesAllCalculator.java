package prizecalc.payout;

public class WinnerTakesAllCalculator implements PayoutCalculator {
    @Override
    public double calculate(int players, double totalPrize) {
        return totalPrize; // Only one winner gets everything
    }
}
