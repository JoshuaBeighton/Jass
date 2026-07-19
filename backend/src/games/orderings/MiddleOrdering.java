package src.games.orderings;

import java.util.Comparator;

import src.objs.Card;

public class MiddleOrdering implements Comparator<Card> {
    private int center;

    public MiddleOrdering(int center) {
        this.center = center;
    }

    @Override
    public int compare(Card c1, Card c2) {
        return Math.abs(c2.getVal() - center) - Math.abs(c1.getVal() - center);
    }
}
