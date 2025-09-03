package prizecalc;

import prizecalc.payout.TieredPayoutCalculator;

import java.util.*;

public class PrizePayoutApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean keepRunning = true;

        while (keepRunning) {
            int playerCount = 0;
            while (playerCount == 0) {
                System.out.print("Enter number of players (non-zero): ");
                playerCount = readPositiveInt(scanner);
            }

            System.out.print("Enter entry price: ");
            double entryPrice = readPositiveDouble(scanner);

            System.out.print("Enter minimum prize: ");
            double minPrize = readPositiveDouble(scanner);

            System.out.print("Enter any additional prize money (sponsors, etc.): ");
            double extraPrize = readPositiveDouble(scanner);

            System.out.print("Enter base logistics cost: ");
            double baseCost = readPositiveDouble(scanner);

            System.out.print("Enter logistics costs per player: ");
            double perPlayerCost = readPositiveDouble(scanner);

            int potGrowth = 0;
            while(potGrowth == 0) {
                System.out.print("Should the pot grow per-player (1), or at round thresholds (2)? ");
                int input = readPositiveInt(scanner);
                if (input == 1 || input == 2) {
                    potGrowth = input;
                } else {
                    System.out.println("Input error: please enter 1 or 2.");
                }
            }
            boolean thresholdRounding = false;
            double maxRound = 0;
            if(potGrowth == 2) {
                thresholdRounding = true;
                System.out.print("Enter the maximum amount to round up the pot size (after entry fees and costs are calculated): ");
                maxRound = readPositiveDouble(scanner);
            }

            int totalPrize = calcPot(playerCount, entryPrice, minPrize, extraPrize, baseCost, perPlayerCost, thresholdRounding, maxRound);
            System.out.println("Total pot: $" + totalPrize + "\n");

            int prizeCutoff = 0;

            while(prizeCutoff == 0) {
                System.out.print("Enter the rank cutoff, the total number of prize recipients (A power of 2 is recommended): ");
                int input = readPositiveInt(scanner);
                if (input > 0 && input <= playerCount) {
                    prizeCutoff = input;
                } else {
                    System.out.println("Input error: number of recipients cannot be 0 or greater than the number of players.");
                }
            }

            int curve = 0;
            while(curve == 0) {
                System.out.print("Should the total payout in each tier be distributed roughly linearly (1), or more heavily favor the top placements (2)? ");
                int input = readPositiveInt(scanner);
                if (input == 1 || input == 2) {
                    curve = input;
                } else {
                    System.out.println("Input error: please enter 1 or 2.");
                }
            }

            double egalitarianism = -1;
            while(egalitarianism == -1) {
                System.out.print("How evenly should the payouts be distributed from 0 to 1? (0 is highly competitive, 1 is very fair. Decimals are ok) ");
                double input = readPositiveDouble(scanner);
                if (input >= 0 && input <= 1) {
                    egalitarianism = input;
                } else {
                    System.out.println("Input error: please enter a value between 0 and 1 inclusive (decimals are valid).");
                }
            }

            double payout = TieredPayoutCalculator.calculate(entryPrice, totalPrize, prizeCutoff, curve, egalitarianism);
            System.out.printf("Calculated payout: $%.2f%n", payout);

            System.out.print("Would you like to calculate again? (y/n): ");
            keepRunning = scanner.next().trim().equalsIgnoreCase("y");
        }

        System.out.println("Thanks for using PrizePayoutApp!");
        scanner.close();
    }

    private static int calcPot(int players, double entryPrice, double minPrize, double extraPrize, double baseCost, double perPlayerCost, boolean thresholdRounding, double maxRound) {
        double pot = (extraPrize - baseCost) + players * (entryPrice - perPlayerCost);
        if(thresholdRounding) {
            if (pot >= 100 && pot < 1000) {
                pot = Math.floor((pot + maxRound) / 100) * 100; //round to nearest 100
            } else if (pot >= 1000 && pot < 10000) {
                pot = Math.floor((pot + maxRound) / 1000) * 1000; //round to nearest 1000
            } else if (pot >= 10000) {
                pot = Math.floor((pot + maxRound) / 5000) * 5000; //round to nearest 5000
            }
        }
        if (pot < minPrize) {
            pot = minPrize;
        }
        return (int) Math.round(pot);
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
                if (value >= 0) return value;
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
                if (value >= 0) return value;
                System.out.print("Please enter a positive amount: ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Enter a valid amount: ");
            }
        }
    }
}
