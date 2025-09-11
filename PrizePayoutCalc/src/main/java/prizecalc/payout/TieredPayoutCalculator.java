package prizecalc.payout;

import java.util.*;
import java.util.stream.Collectors;

public class TieredPayoutCalculator {

    private static final int[] TIERPLAYERCOUNTS = {1, 1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024};
    private static final int[] TIERCUTOFFS = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048};

    public TieredPayoutCalculator() {

    }

    public static double calculate(double entryPrice, int totalPrize, int prizeCutoff, int curve, double egalitarianism) {
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


        List<Integer> idealPayoutTable = new ArrayList<Integer>(prizeCutoff);

        // curve 1: share of total prize for each tier is linear
        if(curve == 1) {
            idealPayoutTable = linearDistribution(tierCount, totalPrize, egalitarianism, entryPrice);
        }
        // curve 2: share of total prize increases as the number of recipients increases for the first third of tiers, then decreases
        else if (curve == 2) {
            idealPayoutTable = curvedDistribution(tierCount, totalPrize, egalitarianism);
        }

        List<Integer> finalPayoutTable = RoundPayouts(idealPayoutTable);
        for (int i = 0; i < finalPayoutTable.size(); i++) {
            System.out.println(i + ": " + finalPayoutTable.get(i));
        }

        return 0;
    }

    private static ArrayList<Integer> linearDistribution(int tierCount, int totalPrize, double egalitarianism, double entryPrice) {
        if (tierCount <= 0 || egalitarianism < 0 || egalitarianism > 1) {
            throw new IllegalArgumentException("Invalid input");
        }

        ArrayList<Double> optimalTierRatios = new ArrayList<>(tierCount);
        double tailMin = Math.max(0.01, 0);

        // head multipliers at extremes
        double H0 = 2.0;             // at egalitarianism=0
        double H1 = 4.0 / 3.0;       // at egalitarianism=1
        double H  = H1 + (1 - egalitarianism) * (H0 - H1);

        // solve extreme-steep (e=0) base value b0 and slope d0
        // sum = (H0 + n-1)*b0 + d0*(n-2)*(n-1)/2 = 1
        // tail = b0 + (n-2)*d0 = tailMin
        // ⇒ b0 = (2 - tailMin*(n-1)) / (n+5)
        double b0 = (2.0 - tailMin * (tierCount - 1)) / (tierCount + 5.0);
        double d0 = (tailMin - b0) / (tierCount - 2.0);

        // interpolate slope for given egalitarianism
        double d = d0 * (1 - egalitarianism);

        // now solve b for general e:
        // (H + n-1)*b + d*(n-2)*(n-1)/2 = 1
        double slopeWeight = (tierCount - 2.0) * (tierCount - 1.0) / 2.0;
        double denom       = H + (tierCount - 1.0);
        double b           = (1.0 - d * slopeWeight) / denom;

        // assemble series
        //ArrayList<Double> series = new ArrayList<>(tierCount);
        optimalTierRatios.add(H * b);                // first
        optimalTierRatios.add(b);                    // second
        for (int k = 2; k < tierCount; k++) {
            optimalTierRatios.add(b + (k - 1) * d);
        }
        for(int j = 0; j < optimalTierRatios.size(); j++) {
            System.out.println(optimalTierRatios.get(j));
        }

        int place = 1;
        List<Integer> optimalTierTotals = new ArrayList<Integer>(tierCount);
        ArrayList<Integer> finalTable = new ArrayList<Integer>(tierCount);
        for (int i = 0; i < tierCount; i++) {
            optimalTierTotals.add((int) Math.round(optimalTierRatios.get(i) * totalPrize));
        }
        for (int tier = 0; tier < tierCount; tier++) {
            for (int i = 0; i < TIERPLAYERCOUNTS[tier]; i++) {
                finalTable.add(optimalTierTotals.get(tier) / TIERPLAYERCOUNTS[tier]);
                System.out.println(place + ": " + optimalTierTotals.get(tier) / TIERPLAYERCOUNTS[tier]);
                place++;
            }
        }
        return finalTable;
    }

    private static ArrayList<Integer> curvedDistribution(int tierCount, int totalPrize, double egalitarianism) {
        List<Double> tempPayouts = new ArrayList<Double>();
        ArrayList<Integer> finalTable = new ArrayList<Integer>(tierCount);
        double exponent = 1.5 - egalitarianism;
        double sum = 0;
        for (int i = 0; i < tierCount; i++) {
            double coefficient = (7.15e-7)*totalPrize*totalPrize+0.04*totalPrize+320;
            double value = coefficient * Math.pow(TIERCUTOFFS[i]+0.5, -exponent);
            for(int g = 0; g < TIERPLAYERCOUNTS[i]; g++) {
                tempPayouts.add(value);
                sum += value;
                //System.out.println(r);
            }
        }
        int place = 1;
        //System.out.println("\n" + sum + "\n");
        for (int i = 0; i < tempPayouts.size(); i++) {
        /*for (int tier = 0; tier < tierCount; tier++) {
            for (int i = 0; i < TIERPLAYERCOUNTS[tier]; i++) {*/
                finalTable.add((int) Math.round(tempPayouts.get(i) / sum * totalPrize)); // ensure values total to totalPrize
                System.out.println(place + ": " + finalTable.get(i));
                place++;
        //    }
        }
        return finalTable;
    }

    private static List<Integer> RoundPayouts(List<Integer> idealPayouts) {
        List<Integer> rounded = new ArrayList<>();

        for (int ideal : idealPayouts) {
            List<Integer> candidates = generateRoundCandidates(ideal);

            int best = ideal;
            int bestUg = ugliness(ideal);
            int bestDist = 0;

            for (int cand : candidates) {
                int ug = ugliness(cand);
                int dist = Math.abs(cand - ideal);

                if (ug < bestUg || (ug == bestUg && dist < bestDist)) {
                    best = cand;
                    bestUg = ug;
                    bestDist = dist;
                }
            }

            rounded.add(best);
        }

        return rounded;
    }

    private static List<Integer> generateRoundCandidates(int ideal) {
        List<Integer> cands = new ArrayList<>();
        String s = Integer.toString(ideal);
        int digits = s.length();

        // For each possible trailing‐zero count
        for (int k = 0; k < digits; k++) {
            int factor = pow10(k);
            int d = ideal / factor;

            // 1) Nearest integer multiple of 1
            cands.add((int) (Math.round((double)d) * factor));

            // 2) Floor and ceil to nearest multiple of 5
            int floor5 = (d / 5) * 5;
            int ceil5  = ((d + 4) / 5) * 5;
            cands.add(floor5 * factor);
            cands.add(ceil5  * factor);
        }

        // Dedupe & filter positives
        return cands.stream()
                .filter(x -> x > 0)
                .distinct()
                .collect(Collectors.toList());
    }

    private static int pow10(int k) {
        int r = 1;
        while (k-- > 0) r *= 10;
        return r;
    }


    // returns a score of how "not round" the given number is.
    private static int ugliness(int x) {
        int zeros = 0;
        while (x % 10 == 0 && x > 0) {
            x /= 10;
            zeros++;
        }
        int prefix = (int)x;
        int digits = (int)Math.log10(prefix) + 1;

        // If prefix is a multiple of 5, it’s “clean”
        if (prefix % 5 == 0) {
            return digits;
        }
        // If we removed at least one zero, that counts as “almost” clean
        if (zeros > 0) {
            return digits + 1;
        }
        // No zeros removed, prefix not multiple of 5 – worst score
        return digits + 1;
    }

}/*That step would likely be made up of, starting from the smallest payout (lowest placements), changing them to the next roundest number in that direction and comparing the new total to the true prize pool. Repeat for each tier and then pick the one with the most roundness that moved closer to the prize pool or, if none of them got closer, repeat with the same initial table but with a */

// error threshold percentage (user defined) instead of zero option. Err on the side of going over, maybe negative to positive slider, perhaps asymmetrical
// bottom payout should be no lower than 1% of the total pot, and no lower than the entry fee