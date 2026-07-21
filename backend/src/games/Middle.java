package src.games;

import java.util.Comparator;
import java.util.List;

import src.objs.Card;
import src.objs.Suit;
import src.games.orderings.MiddleOrdering;

public class Middle implements IGame {
    int center = 10;

    public int getType() {
        return center;
    }


    public String getName() {
        return "Middle";
    }

    public int wins(List<Card> trick, int trickNo) {
        Suit masterSuit = trick.get(0).getSuit();
        int winner = 0;
        Comparator<Card> ordering = new MiddleOrdering(center);
        for (int i = trick.size() - 1; i >= 0; i--) {
            if (trick.get(i).getSuit() == masterSuit && ordering.compare(trick.get(i), trick.get(winner)) >= 0) {
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
