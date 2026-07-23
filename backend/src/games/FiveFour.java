package src.games;

import java.util.List;

import src.objs.Card;

public class FiveFour implements IGame {
    public FiveFour(String start) {
        if (start.equals("top")) {
            type = 0;
        } else
            if (start.equals("bottom")) {
                type = 1;
            } else {
                throw new IllegalArgumentException("Invalid start position for Five Four: " + start);
            }
    }

    private int type;

    public int getType() {
        return type;
    }

    public String getName() {
        return "FiveFour";
    }

    public int wins(List<Card> trick, int trickNo) {
        // Get whether this trick is in the first 5 or the last 4.
        int polarity = trickNo > 4 ? 0 : 1;
        // If in the first 5 of a top down start, or last 4 of a bottom up start, return the winner by top-down.
        if (polarity + type == 1) {
            return new TopDown().wins(trick, trickNo);
        }
        // Otherwise return the winner by bottom up.
        else {
            return new BottomUp().wins(trick, trickNo);

        }
    }


    /**
     * Get the score of a 5-4 trick. If it started top, this uses top-down scoring, otherwise it'll use bottom up scoring.
     */
    public int score(List<Card> cards) {
        if (type == 0) {
            return Scoring.TDScore(cards);
        } else
            if (type == 1) {
                return Scoring.BUScore(cards);
            }
        return 0;
    }
}
