package src.games.orderings;

import java.util.Comparator;

import src.objs.Card;
import src.objs.Suit;

public class Jack9Ordering implements Comparator<Card> {
    private Suit masterSuit;

    public Jack9Ordering(Suit masterSuit) {
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

        // If the first card is a Jack, it wins.
        if (c1.getVal() == 11) {
            return 1;
        }
        // If the second card is a Jack, it wins.
        if (c2.getVal() == 11) {
            return -1;
        }


        // Now we know neither card is a Jack, if either card is a 9, it wins.
        if (c1.getVal() == 9) {
            return 1;
        }
        if (c2.getVal() == 9) {
            return -1;
        }

        // If neither card is a Jack or 9, the highest card wins.
        return c1.getVal() - c2.getVal();

    }

}
