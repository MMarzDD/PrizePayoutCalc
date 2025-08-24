package prizecalc.payout;
//Simple implementation: evenly splits the prize among all players.

public class EqualSplitCalculator implements PayoutCalculator {
    @Override
    public double calculate(int players, double totalPrize) {
        return totalPrize / players;
    }
}
