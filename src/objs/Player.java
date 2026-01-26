package src.objs;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private Team team;

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    private String name;
    private List<Card> cards;

    public void printHand() {
        System.out.println(name);
        for (Card card : cards) {
            System.out.println(card.toString());
        }
        System.out.println("\n");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Player(String name, Team t) {
        this.name = name;
        this.team = t;
        this.cards = new ArrayList<Card>();
    }

    public boolean canPlayCard(Card c, List<Card> played, List<Card> available) {
        boolean hasCard = false;
        for (Card card : cards) {
            if (c.equals(card))
                hasCard = true;
        }
        if (!hasCard) {
            System.out.print("\u001B[2J\u001B[H");
            System.err.println("You don't have that card!");
        }

        if (played.size() > 0 && c.getSuit() != played.get(0).getSuit()) {
            for (Card card : available) {
                if (card.getSuit() == played.get(0).getSuit()) {
                    System.out.print("\u001B[2J\u001B[H");
                    System.err.println("You must follow suit!");
                    return false;
                }
            }
        }
        return true;
    }

    public void removeCard(Card c) {
        int toRemove = -1;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).equals(c))
                toRemove = i;
        }
        System.out.println(toRemove);
        if (toRemove != -1) {
            cards.remove(toRemove);
        }
    }
}
