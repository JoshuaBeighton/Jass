package src.games;

import java.util.Comparator;
import java.util.List;

import src.objs.Card;
import src.objs.Suit;
import src.games.orderings.BottomUpOrdering;

public class BottomUp implements IGame {
    private int type = 0;

    public int getType() {
        return type;
    }

    public String getName() {
        return "Bottom Up";
    }


    public int wins(List<Card> trick, int trickNo) {
        Suit masterSuit = trick.get(0).getSuit();
        int winner = 0;
        Comparator<Card> ordering = new BottomUpOrdering(masterSuit);
        for (int i = 0; i < trick.size(); i++) {
            // Compare each card to the winner at that point, and if the current card beats the winner, update the winner.
            if (ordering.compare(trick.get(i), trick.get(winner)) > 0) {
                winner = i;
            }
        }
        return winner;
    }

    public int score(List<Card> cards) {
        return Scoring.BUScore(cards);
    }
}
