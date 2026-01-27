package src.games;

import java.util.List;

import src.objs.Card;
import src.objs.Suit;

public class BottomUp implements IGame {
    private int type;

    public int getType() {
        return type;
    }

    public String getName() {
        return "Bottom-Up";
    }

    public int wins(List<Card> trick) {
        Suit masterSuit = trick.get(0).getSuit();
        int winner = 0;
        for (int i = 0; i < trick.size(); i++) {
            if (trick.get(i).getSuit() == masterSuit && trick.get(i).getVal() < trick.get(winner).getVal()) {
                winner = i;
            }
        }
        return winner;
    }

    public int score(List<Card> cards) {
        return Scoring.BUScore(cards);
    }
}
