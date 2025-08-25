package prizecalc.payout;

import java.util.*;

public class TieredPayoutCalculator {

    private static final int[] TIERPLAYERCOUNTS = {1, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024};

    public TieredPayoutCalculator() {

    }

    public static double calculate(int prizeCutoff, double totalPrize) {
        // should each winner get a unique prize amount (0) or should ranks be divided into "tiers" with the same prize (1)?
        // // how large is the first prize tier?
        // should the total prize in each tier be roughly equal (0), favor the top few tiers like the top 8 (1), or vastly favor first place (2)?
        // given that curve, how egalitarian should the overall distribution be?

        // count required tiers
        int tierCount = 0;
        int nextHighest = prizeCutoff--;
        // Fill in all bits to the right of the most significant bit.
        // This effectively creates a number like 00001111 for an input like 00001000.
        nextHighest |= nextHighest >> 1;
        nextHighest |= nextHighest >> 2;
        nextHighest |= nextHighest >> 4;
        nextHighest |= nextHighest >> 8;
        nextHighest |= nextHighest >> 16;

        nextHighest++;
        //determines which power of 2 the next highest prize cutoff is by counting the trailing zeros in its binary representation
        //tierCount = Integer.numberOfTrailingZeros(nextHighest) + 1;

        tierCount = Arrays.binarySearch(TIERPLAYERCOUNTS, nextHighest) - 1;

        List<Double> optimalTierRatios = new ArrayList<Double>(tierCount);
        List<Double> optimalTierTotals = new ArrayList<Double>(tierCount);

        // curve 0: share of total prize for each tier is equal (1/tier count)
        for (int i = 0; i < tierCount; i++) {
            optimalTierRatios.add(1.0/tierCount);
        }
        // curve 1: share of total prize increases or is unchanged as the number of recipients increases for the first third of tiers, then decreases or is unchanged
        // curve 2: share of total prize decreases or is unchanged in each tier

        for (int i = 0; i < tierCount; i++) {
            optimalTierTotals.add(optimalTierRatios.get(i) * totalPrize);
        }

        /*int tierSize = 1;
        if (place > 512) {
            tierSize = 512;
        } else if (place > 256) {
            tierSize = 256;
        } else if (place > 128) {
            tierSize = 128;
        } else if (place > 64) {
            tierSize = 64;
        } else if (place > 32) {
            tierSize = 32;
        } else if (place > 16) {
            tierSize = 16;
        } else if (place > 8) {
            tierSize = 8;
        } else if (place > 4) {
            tierSize = 4;
        } else if (place > 2) {
            tierSize = 2;
        }*/
        int place = 1;
        List<Double> payoutTable = new ArrayList<Double>(prizeCutoff);
        for (int tier = 0; tier < tierCount; tier++) {
            for (int i = 0; i < TIERPLAYERCOUNTS[tier]; i++) {
                payoutTable.add(optimalTierTotals.get(tier) / TIERPLAYERCOUNTS[tier]);
                System.out.println(place + ": " + optimalTierTotals.get(tier) / TIERPLAYERCOUNTS[tier]);
                place++;
            }
        }
        return 0;
    }

    // returns a score of how "not round" the given number is.
    private static int ugliness(double number) {
        String numString = Double.toString(number);
        numString = numString.replace(".", "");

        int index = numString.length() - 1;

        // Iterate from the end of the string, removing '0' characters
        while (index >= 0 && numString.charAt(index) == '0') {
            index--;
        }

        // Return the substring up to the first non-zero character (or an empty string if all zeros)
        String prefix = numString.substring(0, index + 1);

        if (Integer.parseInt(prefix) % 5 == 0) { // leading non-zero digits are a multiple of 5
            return prefix.length();
        } else if (index < prefix.length() - 1) { // did we remove at least one zero? if so, is a multiple of 5 when returned
            return prefix.length()+1;
        } else {
            return numString.length(); // the number is not a multiple of 5
        }
    }
}/*That step would likely be made up of, starting from the smallest payout (lowest placements), changing them to the next roundest number in that direction and comparing the new total to the true prize pool. Repeat for each tier and then pick the one with the most roundness that moved closer to the prize pool or, if none of them got closer, repeat with the same initial table but with a */