package src.games;

import java.util.List;

import src.objs.Card;
import src.objs.Suit;

public class Trumps implements IGame {
    private Suit trump;

    public String getName() {
        return "Trumps";
    }

    public void setup(int type) {
        try {
            this.trump = Suit.fromIndex(type);
        } catch (Exception e) {
            System.out.println("Something went wrong!");
        }
    }

    public int wins(List<Card> trick) {
        Suit masterSuit = trick.get(0).getSuit();
        int winner = 0;
        boolean trumped = false;
        for (int i = 0; i < trick.size(); i++) {
            if (trick.get(i).getSuit() == trump) {
                if (trick.get(winner).getSuit() != trump) {
                    winner = i;
                } else {
                    winner = beats(trick.get(winner).getVal(), trick.get(i).getVal()) ? winner : i;
                }
                trumped = true;
            } else if (!trumped) {
                if (trick.get(i).getSuit() == masterSuit && trick.get(i).getVal() > trick.get(winner).getVal()) {
                    winner = i;
                }
            }
        }
        return winner;
    }

    public int score(List<Card> cards) {
        return Scoring.TrumpScore(cards, trump);
    }

    private boolean beats(int trump1, int trump2) {
        if (trump1 == 11) {
            return true;
        } else if (trump1 == 9 && trump2 != 11) {
            return true;
        } else if (trump2 == 9 || trump2 == 11) {
            return false;
        } else {
            return trump1 > trump2;
        }
    }
}
