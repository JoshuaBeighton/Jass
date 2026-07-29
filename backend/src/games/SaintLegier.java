package src.games;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import src.games.orderings.BottomUpOrdering;
import src.games.orderings.MiddleOrdering;
import src.games.orderings.TopDownOrdering;
import src.objs.Card;
import src.objs.Suit;

public class SaintLegier implements IGame {
    private Map<Suit, String> mapping;

    public Map<Suit, String> getMapping() {
        return mapping;
    }

    public SaintLegier(Map<Suit, String> mapping) {
        this.mapping = mapping;
    }

    public int getType() {
        return -1;
    }

    public String getName() {
        return "Saint Legier";
    }

    public int wins(List<Card> trick, int trickNo) {
        Suit masterSuit = trick.get(0).getSuit();
        int winner = 0;
        Comparator<Card> ordering = new MiddleOrdering(10, masterSuit);
        switch (mapping.get(masterSuit).toLowerCase()) {
            case "topdown":
                ordering = new TopDownOrdering(masterSuit);
                break;
            case "bottomup":
                ordering = new BottomUpOrdering(masterSuit);
                break;
            default:
                break;
        }
        for (int i = 0; i < trick.size(); i++) {
            // Compare each card to the winner at that point, and if the current card beats
            // the winner, update the winner.
            if (ordering.compare(trick.get(i), trick.get(winner)) > 0) {
                winner = i;
            }
        }
        return winner;
    }

    public int score(List<Card> cards) {
        return Scoring.TDScore(cards);
    }
}
