package prizecalc.payout;

import java.util.*;

public class TieredPayoutCalculator implements PayoutCalculator {

    private final List<Double> top3Percentages;

    public TieredPayoutCalculator(List<Double> top3Percentages) {
        if (top3Percentages.size() != 3) {
            throw new IllegalArgumentException("Provide exactly 3 percentages for top 3 winners.");
        }
        double total = top3Percentages.stream().mapToDouble(Double::doubleValue).sum();
        if (total >= 100.0) {
            throw new IllegalArgumentException("Top 3 percentages must sum to less than 100.");
        }
        this.top3Percentages = top3Percentages;
    }

    @Override
    public double calculate(int totalPlayers, double totalPrize) {
        int topHalf = (int) Math.ceil(totalPlayers / 2.0);
        int remainingPlayers = topHalf - 3;

        List<Integer> tierSizes = new ArrayList<>(List.of(1, 1, 1));
        int assigned = 0, nextSize = 2;
        while (assigned + nextSize <= remainingPlayers) {
            tierSizes.add(nextSize);
            assigned += nextSize;
            nextSize++;
        }

        double remainingPercent = 100.0 - top3Percentages.stream().mapToDouble(Double::doubleValue).sum();
        List<Double> weights = new ArrayList<>();
        for (int i = 3; i < tierSizes.size(); i++) {
            weights.add(1.0 / tierSizes.get(i));
        }

        double weightSum = weights.stream().mapToDouble(Double::doubleValue).sum();
        List<Double> allPercentages = new ArrayList<>(top3Percentages);
        for (double w : weights) {
            allPercentages.add((w / weightSum) * remainingPercent);
        }

        double totalUsed = 0.0, totalIntended = 0.0;
        int currentRank = 1;

        System.out.println("\n📊 Tiered Payout Breakdown:");
        for (int i = 0; i < tierSizes.size(); i++) {
            int size = tierSizes.get(i);
            double percent = allPercentages.get(i);
            double payout = totalPrize * (percent / 100.0);
            double rounded = Math.floor(payout / 100.0) * 100;
            totalIntended += payout;
            totalUsed += rounded;

            String label = (i < 3) ? ordinal(i + 1) + " place" : "Tier " + (i + 1);
            System.out.printf("  %s (Ranks %d–%d, %d player%s, %.2f%%): $%.2f%n",
                    label, currentRank, currentRank + size - 1, size, size > 1 ? "s" : "", percent, rounded);

            currentRank += size;
        }

        if (currentRank <= totalPlayers) {
            System.out.printf("  Bottom %d players (Ranks %d–%d): $0.00%n",
                    totalPlayers - currentRank + 1, currentRank, totalPlayers);
        }

        double remainder = totalPrize - totalUsed;
        double roundingLoss = totalIntended - totalUsed;
        System.out.printf("  💡 Unallocated remainder: $%.2f%n", remainder);
        System.out.printf("  ⚠️ Unawarded due to rounding: $%.2f%n", roundingLoss);

        return totalUsed;
    }

    private String ordinal(int i) {
        return switch (i) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> i + "th";
        };
    }
}