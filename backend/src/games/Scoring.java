package src.games;

import java.util.List;

import src.objs.Card;
import src.objs.Suit;

public class Scoring {
    /**
     * Get the score of a list of cards under a top-down scoring mechanism.
     * @param cards The list of cards to score.
     * @return The total value of those cards.
     */
    public static int TDScore(List<Card> cards) {
        int score = 0;
        // Add the score of each card.
        for (Card c : cards) {
            score += TDScore(c);
        }
        return score;
    }

    /**
     * Get the score of an individual card under a top-down scoring mechanism.
     * @param card The card to get a score for.
     * @return The value of that card.
     */
    public static int TDScore(Card card) {
        switch (card.getVal()) {
            case 14:
                return 11;
            case 13:
                return 4;
            case 12:
                return 3;
            case 11:
                return 2;
            case 10:
                return 10;
            case 8:
                return 8;
        }
        return 0;
    }

    /**
     * Get the score of a list of cards under a bottom-up scoring mechanism.
     * @param cards The list of cards to score.
     * @return The total value of those cards.
     */
    public static int BUScore(List<Card> cards) {
        int score = 0;
        for (Card c : cards) {
            score += BUScore(c);
        }
        return score;
    }

    /**
     * Get the score of an individual card under a bottom-up scoring mechanism.
     * @param card The card to get a score for.
     * @return The value of that card.
     */
    public static int BUScore(Card card) {
        switch (card.getVal()) {
            case 6:
                return 11;
            case 13:
                return 4;
            case 12:
                return 3;
            case 11:
                return 2;
            case 10:
                return 10;
            case 8:
                return 8;
        }
        return 0;
    }

    /**
     * Get the score of a list of cards under a trumps scoring mechanism.
     * @param cards The list of cards to score.
     * @param trumpSuit The suit of trumps.
     * @return The total value of those cards.
     */
    public static int TrumpScore(List<Card> cards, Suit trumpSuit) {
        int score = 0;
        for (Card c : cards) {
            score += TrumpScore(c, trumpSuit);
        }
        return score;
    }

    /**
     * Get the score of an individual card under a trumps scoring mechanism.
     * @param card The card to get a score for.
     * @param trumpSuit The suit of trumps.
     * @return The value of that card.
     */
    public static int TrumpScore(Card card, Suit trumpSuit) {
        switch (card.getVal()) {
            case 14:
                return 11;
            case 13:
                return 4;
            case 12:
                return 3;
            // The Jack of Trumps scores 20, the Jack of other suits scores 2.
            case 11:
                if (card.getSuit() == trumpSuit) {
                    return 20;
                } else {
                    return 2;
                }
            case 10:
                return 10;
            // The 9 of Trumps scores 14, the 9 of other suits scores nothing.
            case 9:
                if (card.getSuit() == trumpSuit) {
                    return 14;
                }
        }
        return 0;
    }
}
