package src.utils;

import java.util.Comparator;

import src.objs.Card;

/**
 * Comparator used to sort cards first by suit, then by descending value.
 *
 * This ordering is used to display a player's hand in a consistent format.
 */
public class CardComparator implements Comparator<Card> {
    /**
     * Compares two cards by suit index, then by value in reverse order.
     *
     * @param c1 the first card
     * @param c2 the second card
     * @return a negative integer, zero, or a positive integer as the first card is less than, equal to, or greater than the
     *         second card
     */
    public int compare(Card c1, Card c2) {
        if (c1.getSuit().index() > c2.getSuit().index()) {
            return 1;
        }

        if (c1.getSuit().index() < c2.getSuit().index()) {
            return -1;
        }

        return c2.getVal() - c1.getVal();
    }
}
