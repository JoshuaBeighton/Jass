package src.games;

import java.util.List;

import src.objs.Card;
import src.objs.Suit;

public class Middle implements IGame {
    int center = 10;

    public int getType() {
        return center;
    }


    public String getName() {
        return "Middle";
    }

    public int wins(List<Card> trick) {
        Suit masterSuit = trick.get(0).getSuit();
        int winner = 0;
        for (int i = trick.size() - 1; i >= 0; i--) {
            if (trick.get(i).getSuit() == masterSuit && (Math.abs(trick.get(i).getVal() - center) <= Math.abs(trick.get(winner).getVal() - center))) {
                winner = i;
            }
        }
        return winner;
    }

    public int score(List<Card> cards) {
        return Scoring.TDScore(cards);
    }
}
