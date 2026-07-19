package src.games.orderings;

import java.util.Comparator;

import src.objs.Card;

public class TrumpOrdering implements Comparator<Card> {
    @Override
    public int compare(Card c1, Card c2) {
        if (c1.getVal() == 11) {
            return 1;
        } else
            if (c1.getVal() == 9 && c2.getVal() != 11) {
                return 1;
            } else
                if (c2.getVal() == 9 || c2.getVal() == 11) {
                    return -1;
                } else {
                    return c1.getVal() - c2.getVal();
                }
    }

}
