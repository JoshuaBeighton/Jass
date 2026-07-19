package src.objs;

import javax.naming.InvalidNameException;

import java.util.HashMap;
import java.util.Map;

import src.utils.Utils;

public class Card {
    private Suit suit;
    private int val;

    public Card(Suit suit, int val) {
        this.suit = suit;
        this.val = val;
    }

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public boolean equals(Card c2) {
        return (suit.index() == c2.suit.index()) && (val == c2.val);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) {
            sb.append("\u001B[31m");
        }
        sb.append(Utils.getCardVal(val));
        sb.append(Utils.getCharFromSuit(suit));
        sb.append("\u001B[0m");
        return sb.toString();
    }

    public static Card parseCard(String s) throws InvalidNameException {
        Suit suit = Utils.getSuitFromChar(s.charAt(s.length() - 1));

        try {
            int val;
            switch (s.charAt(0)) {
                case 'j', 'J':
                    val = 11;
                    break;
                case 'q', 'Q':
                    val = 12;
                    break;
                case 'k', 'K':
                    val = 13;
                    break;
                case 'a', 'A':
                    val = 14;
                    break;
                default:
                    val = Integer.parseInt(s.substring(0, s.length() - 1));
                    break;
            }
            Card c = new Card(suit, val);
            return c;
        }
        catch (Exception e) {
            throw new InvalidNameException();
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("suit", suit.name());
        result.put("number", val);
        return result;
    }
}
