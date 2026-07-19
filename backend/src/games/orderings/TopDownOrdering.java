package src.games.orderings;

import java.util.Comparator;

import src.objs.Card;

public class TopDownOrdering implements Comparator<Card> {
    @Override
    public int compare(Card c1, Card c2) {
        return c1.getVal() - c2.getVal();
    }
}
