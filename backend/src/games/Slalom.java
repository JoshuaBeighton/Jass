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

    public int wins(List<Card> trick, int trickNo) {
        int polarity = trickNo % 2;
        if (polarity + type == 1) {
            return new BottomUp().wins(trick, trickNo);
        } else {
            return new TopDown().wins(trick, trickNo);
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
        return -1; // Slalom does not have a specific suit
    }
}
