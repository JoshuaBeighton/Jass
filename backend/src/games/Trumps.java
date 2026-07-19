package src.games;

import java.util.List;

import src.games.orderings.TrumpOrdering;
import src.objs.Card;
import src.objs.Suit;

public class Trumps implements IGame {
    private Suit trump;

    public Trumps(Suit s) {
        this.trump = s;
    }

    public int getType() {
        return trump.index();
    }

    public String getName() {
        return "Trumps";
    }

    public void setup(int type) {
        try {
            this.trump = Suit.fromIndex(type);
        }
        catch (Exception e) {
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
                    TrumpOrdering ordering = new TrumpOrdering();
                    winner = ordering.compare(trick.get(winner), trick.get(i)) == 1 ? winner : i;
                }
                trumped = true;
            } else
                if (!trumped) {
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

    public int getSuit() {
        return trump.index();
    }
}
