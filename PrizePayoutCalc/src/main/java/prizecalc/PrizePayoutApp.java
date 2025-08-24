package prizecalc;

import prizecalc.payout.PayoutCalculator;
import prizecalc.payout.EqualSplitCalculator;
import prizecalc.payout.TieredPayoutCalculator;
import prizecalc.payout.WinnerTakesAllCalculator;

import java.util.*;

public class PrizePayoutApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean keepRunning = true;

        while (keepRunning) {
            System.out.println("Choose payout strategy:");
            System.out.println("1 - Equal Split");
            System.out.println("2 - Winner Takes All");
            System.out.println("3 - Tiered Payout (Custom percentages for top players)");
            System.out.print("Enter choice (1, 2 or 3): ");
            String choice = scanner.next().trim();

            System.out.print("Enter number of players: ");
            int players = readPositiveInt(scanner);

            System.out.print("Enter total prize amount ($): ");
            double totalPrize = readPositiveDouble(scanner);

            PayoutCalculator calculator;

            switch (choice) {
                case "1":
                    calculator = new EqualSplitCalculator();
                    break;
                case "2":
                    calculator = new WinnerTakesAllCalculator();
                    break;
                case "3":
                    calculator = buildTieredCalculator(scanner, players); // ✅ Now passes player count
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
                    continue;
            }

            double payout = calculator.calculate(players, totalPrize);
            System.out.printf("Calculated payout: $%.2f%n", payout);

            System.out.print("Would you like to calculate again? (yes/no): ");
            keepRunning = scanner.next().trim().equalsIgnoreCase("yes");
        }

        System.out.println("Thanks for using PrizePayoutApp!");
        scanner.close();
    }

    private static PayoutCalculator buildTieredCalculator(Scanner scanner, int totalPlayers) {
        System.out.println("Enter custom percentages for the top 3 winners:");

        List<Double> top3 = new ArrayList<>();
        double totalTop3 = 0.0;

        for (int i = 0; i < 3; i++) {
            System.out.printf("  %s place: ", ordinal(i + 1));
            double percent = readPositiveDouble(scanner);
            top3.add(percent);
            totalTop3 += percent;
        }

        if (totalTop3 >= 100.0) {
            System.out.printf("❌ Error: Top 3 percentages must sum to less than 100. You entered %.2f%%.%n", totalTop3);
            return buildTieredCalculator(scanner, totalPlayers); // Retry
        }

        return new TieredPayoutCalculator(top3);
    }

    private static String ordinal(int i) {
        return switch (i) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> i + "th";
        };
    }

    private static int readPositiveInt(Scanner scanner) {
        while (true) {
            try {
                int value = Integer.parseInt(scanner.next());
                if (value > 0) return value;
                System.out.print("Please enter a positive integer: ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Enter a valid number: ");
            }
        }
    }

    private static double readPositiveDouble(Scanner scanner) {
        while (true) {
            try {
                double value = Double.parseDouble(scanner.next());
                if (value > 0) return value;
                System.out.print("Please enter a positive amount: ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Enter a valid amount: ");
            }
        }
    }
}
