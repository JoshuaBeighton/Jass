package src.games;

import java.util.List;

import src.objs.Card;

public class Slalom implements IGame {
    public Slalom(String start) {
        if (start.equals("top")) {
            type = 0;
        } else
            if (start.equals("bottom")) {
                type = 1;
            } else {
                throw new IllegalArgumentException("Invalid start position for Slalom: " + start);
            }
    }

    private int type;

    public int getType() {
        return type;
    }

    public String getName() {
        return "Slalom";
    }

    /**
     * Determine who wins a slalom trick.
     * @param trick The ordered list of cards.
     * @param trickNo Which number trick this is - this is needed for knowing which ordering to use.
     * @return The winning index of the trick.
     */
    public int wins(List<Card> trick, int trickNo) {
        // Get whether the trick is odd or even numbered.
        int polarity = trickNo % 2;
        // If it's even numbered for a bottom up, or odd numbered for a top down, use a bottom up ordering.
        if (polarity + type == 1) {
            return new BottomUp().wins(trick, trickNo);
        }
        // Otherwise use a top-down ordering.
        else {
            return new TopDown().wins(trick, trickNo);
        }
    }

    /**
     * Get the score of a slalom trick. If it started top, this uses top-down scoring, otherwise it'll use bottom up
     * scoring.
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
