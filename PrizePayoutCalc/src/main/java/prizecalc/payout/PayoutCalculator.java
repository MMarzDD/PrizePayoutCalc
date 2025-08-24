package prizecalc.payout;
//Defines the contract for any payout strategy.

public interface PayoutCalculator {
    double calculate(int players, double totalPrize);
}
