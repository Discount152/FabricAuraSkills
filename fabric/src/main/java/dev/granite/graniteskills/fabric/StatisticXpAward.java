package dev.granite.graniteskills.fabric;

/**
 * Statistic XP for one poll. An increase below the source minimum is kept for the next poll.
 * No increase, or a decrease, awards nothing and does not move the baseline.
 */
public final class StatisticXpAward {

    public record Result(double xp, boolean advanceBaseline) {
    }

    private StatisticXpAward() {
    }

    public static Result award(int current, int last, int minimumIncrease, double multiplier, double sourceXp) {
        int change = current - last;
        if (change <= 0 || change < minimumIncrease) {
            return new Result(0.0, false);
        }
        return new Result(change * multiplier * sourceXp, true);
    }
}
