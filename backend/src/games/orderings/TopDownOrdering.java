package src.games.orderings;

import java.util.Comparator;

import src.objs.Card;
import src.objs.Suit;

public class TopDownOrdering implements Comparator<Card> {
    private Suit masterSuit;

    public TopDownOrdering(Suit masterSuit) {
        this.masterSuit = masterSuit;
    }

    @Override
    public int compare(Card c1, Card c2) {
        // Check that at least one of the cards is of the right suit.
        if (!(c1.getSuit().equals(masterSuit) || c2.getSuit().equals(masterSuit))) {
            System.err.println("Something went wrong: Comparing two cards, neither of which is the master suit.");
            return 0;
        }

        // If the first card being compared is not of the master suit, the second card wins.
        if (!c1.getSuit().equals(masterSuit)) {
            return -1;
        }

        // Likewise, if the second card being compared is not of the master suit, the first card wins.
        if (!c2.getSuit().equals(masterSuit)) {
            return 1;
        }

        // If they are both of the right suit, then simply subtract to get whether the first or second card has a higher value.
        return c1.getVal() - c2.getVal();
    }
}
