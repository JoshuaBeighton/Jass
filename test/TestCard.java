package test;

import src.objs.Card;
import src.objs.Suit;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestCard {
    @Test
    public void testEqualityClubs(){
        Card c1 = new Card(Suit.CLUBS, 7);
        Card c2 = new Card(Suit.CLUBS, 7);
        assertTrue(c1.equals(c2));
    }


    @Test
    public void testEqualityHearts(){
        Card c1 = new Card(Suit.HEARTS, 7);
        Card c2 = new Card(Suit.HEARTS, 7);
        assertTrue(c1.equals(c2));
    }


    @Test
    public void testEqualityDiamonds(){
        Card c1 = new Card(Suit.DIAMONDS, 7);
        Card c2 = new Card(Suit.DIAMONDS, 7);
        assertTrue(c1.equals(c2));
    }

    @Test
    public void testEqualitySpades(){
        Card c1 = new Card(Suit.SPADES, 7);
        Card c2 = new Card(Suit.SPADES, 7);
        assertTrue(c1.equals(c2));
    }
}
