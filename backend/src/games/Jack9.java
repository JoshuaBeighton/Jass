package src.games;

import java.util.List;

import src.games.orderings.Jack9Ordering;
import src.objs.Card;
import src.objs.Suit;

public class Jack9 implements IGame {
    private int type;

    public int getType() {
        return type;
    }

    public String getName() {
        return "Jack9";
    }

    public int wins(List<Card> trick, int trickNo) {
        Suit masterSuit = trick.get(0).getSuit();
        int winner = 0;
        Jack9Ordering ordering = new Jack9Ordering(masterSuit);
        for (int i = 0; i < trick.size(); i++) {
            if (ordering.compare(trick.get(i), trick.get(winner)) > 0) {
                winner = i;
            }
        }
        return winner;
    }

    public int score(List<Card> cards) {
        return Scoring.TDScore(cards);
    }
}
