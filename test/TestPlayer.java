package test;

import src.objs.Card;
import src.objs.Suit;
import src.objs.Team;
import src.objs.Player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestPlayer {
    @Test
    public void testAddCard1(){
        Card c1 = new Card(Suit.HEARTS, 7);

        Player p = new Player("Jim", new Team());

        assertEquals(p.getCards().size(), 0);

        p.getCards().add(c1);

        assertEquals(p.getCards().size(), 1);
    }


    @Test
    public void testRemoveCard1(){
        Card c1 = new Card(Suit.HEARTS, 7);
        Card c2 = new Card(Suit.HEARTS, 7);

        Player p = new Player("Jim", new Team());

        assertEquals(p.getCards().size(), 0);

        p.getCards().add(c1);

        assertEquals(p.getCards().size(), 1);

        p.removeCard(c2);

        assertEquals(p.getCards().size(), 0);
    }


    @Test
    public void testRemoveCard2(){
        Card c1 = new Card(Suit.CLUBS, 14);
        Card c2 = new Card(Suit.CLUBS, 14);

        Player p = new Player("Jim", new Team());

        assertEquals(p.getCards().size(), 0);

        p.getCards().add(c1);

        assertEquals(p.getCards().size(), 1);

        p.removeCard(c2);

        assertEquals(p.getCards().size(), 0);
    }
}
