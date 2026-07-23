package src.games;

import java.util.List;

import src.games.orderings.TopDownOrdering;
import src.games.orderings.Jack9Ordering;
import src.objs.Card;
import src.objs.Suit;

public class Trumps implements IGame {
    private Suit trump;

    /**
     * Setup a new Trumps game with a given trump suit.
     * @param s The suit that will be trumps.
     */
    public Trumps(Suit s) {
        this.trump = s;
    }

    /**
     * @return the index of the trump suit.
     */
    public int getType() {
        return trump.index();
    }

    public String getName() {
        return "Trumps";
    }

    public int wins(List<Card> trick, int trickNo) {
        // Get the suit of the first card played. The only suit to beat this is trumps.
        Suit masterSuit = trick.get(0).getSuit();
        // Store the winning index.
        int winner = 0;
        // Store whether a trump has been played.
        boolean trumped = false;

        // Loop through the trick.
        for (int i = 0; i < trick.size(); i++) {
            // If this card is a trump.
            if (trick.get(i).getSuit() == trump) {
                // If the current winner isn't a trump, this card beats it.
                if (trick.get(winner).getSuit() != trump) {
                    winner = i;
                }
                // Otherwise, determine which is the higher value in the trump ordering, and set the winner to that card.
                else {
                    Jack9Ordering ordering = new Jack9Ordering(trump);
                    winner = ordering.compare(trick.get(winner), trick.get(i)) == 1 ? winner : i;
                }
                // Now, a trump suit has been played.
                trumped = true;
            }
            // If this card is not a trump, and no trumps have been played.
            else
                if (!trumped) {
                    // Get a new top down ordering.
                    TopDownOrdering ordering = new TopDownOrdering(masterSuit);
                    // If the card beats the current winner, update the winner.
                    if (ordering.compare(trick.get(i), trick.get(winner)) > 0) {
                        winner = i;
                    }
                }
        }
        // Return the winning index.
        return winner;
    }

    public int score(List<Card> cards) {
        return Scoring.TrumpScore(cards, trump);
    }
}
