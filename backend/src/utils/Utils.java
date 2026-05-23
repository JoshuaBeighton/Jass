package src.utils;

import javax.naming.InvalidNameException;

import src.objs.Suit;

public class Utils {
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
