package src.games;

import java.util.List;

import src.objs.Card;

public interface IGame {
    /**
     * 
     * @return The name of the game being played.
     */
    public String getName();

    /**
     * Get the type of game - this field has various uses depending on the game mode. For trumps, this will return the trump
     * suit. For slalom / 5-4, this returns whether the start trick was top (0) or bottom (1)
     * @return The type of game.
     */
    public int getType();

    /**
     * Determine who wins a trick.
     * @param trick an ordered list of cards in the trick.
     * @param trickNo the number trick that it is - some game modes change their win condition between tricks.
     * @return The index in the list of the winning card.
     */
    public int wins(List<Card> trick, int trickNo);

    /**
     * Calculate the score of a trick.
     * @param trick a list of cards in the trick.
     * @return The totalled scoring of the trick.
     */
    public int score(List<Card> cards);
}
