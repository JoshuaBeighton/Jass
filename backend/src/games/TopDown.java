package src.games;

import java.util.List;

import src.games.orderings.TopDownOrdering;
import src.objs.Card;
import src.objs.Suit;

public class TopDown implements IGame {
    private int type;

    public int getType() {
        return type;
    }

    public String getName() {
        return "Top Down";
    }

    public int wins(List<Card> trick, int trickNo) {
        Suit masterSuit = trick.get(0).getSuit();
        int winner = 0;
        TopDownOrdering ordering = new TopDownOrdering();
        for (int i = 0; i < trick.size(); i++) {
            if (trick.get(i).getSuit() == masterSuit && ordering.compare(trick.get(i), trick.get(winner)) > 0) {
                winner = i;
            }
        }
        return winner;
    }

    public int score(List<Card> cards) {
        return Scoring.TDScore(cards);
    }

    public int getSuit() {
        return -1;
    }
}
