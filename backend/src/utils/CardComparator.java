package src.utils;
import java.util.Comparator;

import src.objs.Card;

public class CardComparator implements Comparator<Card>{
    public int compare(Card c1, Card c2){
        if (c1.getSuit().index() > c2.getSuit().index()){
            return 1;
        }

        if (c1.getSuit().index() < c2.getSuit().index()){
            return -1;
        }

        return c2.getVal() - c1.getVal();
    }    
}