package src.objs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import src.games.Elephant;
import src.games.IGame;
import src.games.Rio;
import src.games.Trumps;
import src.games.orderings.Jack9Ordering;

/**
 * Represents a player in a Jass game.
 *
 * A player has a name, a team, and a hand of cards.
 */
public class Player {
    /** The team this player belongs to. */
    private Team team;

    /** The player's display name. */
    private String name;

    /** The cards currently held by the player. */
    private List<Card> cards;

    /**
     * Creates a new player with a fixed name and team.
     *
     * @param name the player's name
     * @param t the team to assign
     */
    public Player(String name, Team t) {
        this.name = name;
        this.team = t;
        this.cards = new ArrayList<Card>();
    }

    /**
     * Returns the player's team.
     *
     * @return the team
     */
    public Team getTeam() {
        return team;
    }

    /**
     * Assigns the player to a team.
     *
     * @param team the new team
     */
    public void setTeam(Team team) {
        this.team = team;
    }

    /**
     * Prints the player's hand to the server console.
     */
    public void printHand() {
        System.out.println(name);
        for (Card card : cards) {
            System.out.println(card.toString());
        }
        System.out.println("\n");
    }

    /**
     * Returns the player's name.
     *
     * @return the player name
     */
    public String getPlayerName() {
        return name;
    }

    /**
     * Sets the player's name.
     *
     * @param name the new name
     */
    public void setPlayerName(String name) {
        this.name = name;
    }

    /**
     * Returns the player's current cards.
     *
     * @return the hand of cards
     */
    public List<Card> getCards() {
        return cards;
    }

    /**
     * Replaces the player's hand.
     *
     * @param cards the new card list
     */
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Map<Card, Boolean> playable(List<Card> played, IGame game) {
        Map<Card, Boolean> result = new HashMap<Card, Boolean>();
        for (Card card : cards) {
            result.put(card, canPlayCard(card, played, game));
        }
        return result;
    }

    /**
     * Determines whether the player may legally play the specified card.
     *
     * The player must hold the card, and must follow the rules for trumps and suit-following.
     *
     * @param c the card to play
     * @param played the cards already played in the current trick
     * @param trump the trump suit index, or -1 if not a trumps game
     * @return true if the play is legal, false otherwise
     */
    public boolean canPlayCard(Card c, List<Card> played, IGame game) {
        // Ensure the player has the card they are trying to play in their hand.
        boolean hasCard = false;
        for (Card card : cards) {
            if (c.equals(card))
                hasCard = true;
        }
        if (!hasCard) {
            return false;
        }

        // If this is the first card in the trick, they can play any card.
        if (played.size() == 0) {
            return true;
        }

        // If they followed suit, they can always play that card.
        Suit masterSuit = played.get(0).getSuit();
        if (c.getSuit() == masterSuit)
            return true;

        // Handle trumps or elephant in the trump stage.
        if (game instanceof Trumps || (game instanceof Elephant && game.getType() != -1)) {
            Suit trumpSuit = Suit.fromIndex(game.getType());

            // If they are playing a trump.
            if (c.getSuit() == trumpSuit) {
                // If it beats all the trumps played so far.
                boolean beats = true;
                Jack9Ordering ordering = new Jack9Ordering(trumpSuit);
                for (Card card : played) {
                    if (ordering.compare(card, c) > 0) {
                        beats = false;
                    }
                }
                if (beats) {
                    return true;
                }
            }
        }
        // Handle rio
        else if (game instanceof Rio) {
            String color = game.getType() == 0 ? "red" : "black";

            // Get the trump suit of the current trick.
            Suit trumpSuit = null;
            for (int i = played.size() - 1; i > 0; i--) {
                if (played.get(i).getSuit().getColor().equals(color)) {
                    trumpSuit = played.get(i).getSuit();
                }
            }

            // If trumping for the first time, return true
            if (trumpSuit == null && c.getSuit().getColor().equals(color))
                return true;


            // If playing a trump against a current trump.
            if (c.getSuit() == trumpSuit) {
                boolean beats = true;
                Jack9Ordering ordering = new Jack9Ordering(trumpSuit);
                for (Card card : played) {
                    if (ordering.compare(card, c) > 0) {
                        beats = false;
                    }
                }
                if (beats) {
                    return true;
                }
            }
        }
        // Default case

        for (Card available : cards) {
            if (available.getSuit() == masterSuit) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes the first matching card from the player's hand.
     *
     * @param c the card to remove
     */
    public void removeCard(Card c) {
        int toRemove = -1;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).equals(c))
                toRemove = i;
        }
        if (toRemove != -1) {
            cards.remove(toRemove);
        }
    }

    /**
     * Converts the player into a map representation for JSON serialization.
     *
     * @return a map containing the player's name and team index
     */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("name", name);
        m.put("team", team.getIndex());

        return m;
    }
}
