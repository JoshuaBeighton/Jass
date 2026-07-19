package src.games.orderings;

import java.util.Comparator;

import src.objs.Card;

public class BottomUpOrdering implements Comparator<src.objs.Card> {
    @Override
    public int compare(Card c1, Card c2) {
        return c2.getVal() - c1.getVal();
    }

}
