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
        int polarity = trickNo > 4 ? 0 : 1;
        if (polarity + type == 1) {
            return new TopDown().wins(trick, trickNo);
        } else {
            return new BottomUp().wins(trick, trickNo);

        }
    }

    public int score(List<Card> cards) {
        if (type == 0) {
            return Scoring.TDScore(cards);
        } else
            if (type == 1) {
                return Scoring.BUScore(cards);
            }
        return 0;
    }

    public int getSuit() {
        return -1; // Five Four does not have a specific suit
    }
}
