package src.objs;

import javax.naming.InvalidNameException;

import java.util.HashMap;
import java.util.Map;

import src.utils.Utils;

/**
 * Represents a playing card for the Jass game.
 *
 * A card is defined by its suit and its numeric value. Special ranks such as Jack, Queen, King, and Ace are represented
 * by the values 11 through 14.
 */
public class Card {
    // The suit of the card (HEARTS, DIAMONDS, CLUBS, SPADES).
    private Suit suit;

    // The numeric value of the card. Face cards use 11-14.
    private int val;

    /**
     * Creates a new card with the given suit and value.
     *
     * @param suit the suit of the card
     * @param val the numeric value of the card
     */
    public Card(Suit suit, int val) {
        this.suit = suit;
        this.val = val;
    }

    /**
     * Returns the suit of this card.
     *
     * @return the card suit
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Updates the suit of this card.
     *
     * @param suit the new card suit
     */
    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    /**
     * Returns the numeric value of this card.
     *
     * @return the card value
     */
    public int getVal() {
        return val;
    }

    /**
     * Updates the numeric value of this card.
     *
     * @param val the new card value
     */
    public void setVal(int val) {
        this.val = val;
    }

    /**
     * Compares this card with another card using suit and value.
     *
     * @param c2 the card to compare against
     * @return true if both suit and value match, false otherwise
     */
    public boolean equals(Card c2) {
        return (suit.index() == c2.suit.index()) && (val == c2.val);
    }

    /**
     * Returns a colored string representation of the card for console output. Red text is used for hearts and diamonds.
     *
     * @return the formatted card string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Use red coloring for red suits in terminal output.
        if (suit == Suit.HEARTS || suit == Suit.DIAMONDS) {
            sb.append("\u001B[31m");
        }

        sb.append(Utils.getCardVal(val));
        sb.append(Utils.getCharFromSuit(suit));
        sb.append("\u001B[0m");
        return sb.toString();
    }

    /**
     * Parses a string into a Card object.
     *
     * Accepted formats include numeric values followed by suit character ("9H" for nine of hearts) and face cards like
     * "JH", "QD", "KS", "AC".
     *
     * @param s the string to parse
     * @return a Card instance matching the parsed value and suit
     * @throws InvalidNameException if parsing fails for any reason
     */
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

    /**
     * Converts this card into a map representation for JSON serialization.
     *
     * @return a map containing suit and number keys
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("suit", suit.name());
        result.put("number", val);
        return result;
    }
}
