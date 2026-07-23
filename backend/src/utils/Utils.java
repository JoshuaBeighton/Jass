package src.utils;

import javax.naming.InvalidNameException;

import src.objs.Suit;

/**
 * Utility methods for card and suit formatting.
 */
public class Utils {
    /**
     * Returns the printable symbol for the given suit.
     *
     * @param s the suit to convert
     * @return suit symbol character
     */
    public static char getCharFromSuit(Suit s) {
        switch (s) {
            case Suit.HEARTS:
                return '♥';
            case Suit.DIAMONDS:
                return '♦';
            case Suit.SPADES:
                return '♠';
            default:
                return '♣';
        }
    }

    /**
     * Converts a suit character or symbol to the corresponding Suit enum.
     *
     * Accepts both upper- and lowercase letters, as well as suit glyphs.
     *
     * @param c the suit character or symbol
     * @return the matching Suit
     * @throws InvalidNameException when the input is not a valid suit
     */
    public static Suit getSuitFromChar(char c) throws InvalidNameException {
        if (c >= 'A' && c <= 'Z') {
            c -= 'A';
            c += 'a';
        }
        switch (c) {
            case 'h', '♥':
                return Suit.HEARTS;
            case 'd', '♦':
                return Suit.DIAMONDS;
            case 'c', '♣':
                return Suit.CLUBS;
            case 's', '♠':
                return Suit.SPADES;
            default:
                throw new InvalidNameException();
        }
    }

    /**
     * Returns the string representation for a card value.
     *
     * Face values are returned as letters while numbered cards are returned as digits.
     *
     * @param val the numeric card value
     * @return string form of the card value
     */
    public static String getCardVal(int val) {
        switch (val) {
            case 11:
                return "J";
            case 12:
                return "Q";
            case 13:
                return "K";
            case 14:
                return "A";
            default:
                return String.valueOf(val);
        }
    }
}
