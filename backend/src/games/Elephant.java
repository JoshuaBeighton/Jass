package src.games;

import java.util.List;

import src.objs.Card;
import src.objs.Suit;

public class Elephant implements IGame {
    private Suit trumpSuit;

    public int getType() {
        if (trumpSuit == null) {
            return -1;
        }
        return trumpSuit.index();
    }

    public String getName() {
        return "Elephant";
    }

    public int wins(List<Card> trick, int trickNo) {
        // Get which set of 3 this card belongs to.
        int polarity = trickNo / 3;
        // If in the first 5 of a top down start, or last 4 of a bottom up start, return the winner by top-down.
        if (polarity == 0) {
            return new TopDown().wins(trick, trickNo);
        }
        // Otherwise if in the second group of 3, play as bottom up.
        else if (polarity == 1) {
            return new BottomUp().wins(trick, trickNo);
        }
        // Otherwise play as trumps.
        else {
            return new Trumps(trumpSuit).wins(trick, trickNo);
        }
    }

    public void setTrump(Suit trumpSuit) {
        this.trumpSuit = trumpSuit;
    }

    public int score(List<Card> cards) {
        return Scoring.TDScore(cards);
    }
}
