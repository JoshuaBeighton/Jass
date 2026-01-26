package src;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import src.objs.Card;
import src.objs.Player;
import src.objs.Suit;
import src.objs.Team;
import src.utils.CardComparator;

public class Main {
    private static List<Card> undealt;
    private static List<Player> players;

    public static void main(String[] args) {
        System.out.print("\u001B[2J\u001B[H");
            System.out.flush();
        fillDeck();
        Team t1 = new Team();
        Team t2 = new Team();

        players = new ArrayList<Player>();
        players.add(new Player("Jim", t1));
        players.add(new Player("Paul", t2));
        players.add(new Player("Steve", t1));
        players.add(new Player("Andy", t2));

        for (Player p : players) {
            p.getTeam().players.add(p);
        }

        Collections.shuffle(undealt);
        dealCards();
        sortCards();
        GameManager m = new GameManager(players);
        m.playGame();
    }

    private static void dealCards() {
        int playerIndex = 0;
        while (!undealt.isEmpty()) {
            players.get(playerIndex).getCards().addAll(undealt.subList(0, 3));
            undealt.removeAll(undealt.subList(0, 3));
            playerIndex++;
            if (playerIndex >= players.size())
                playerIndex = 0;
        }
    }

    private static void sortCards(){
        for (Player player : players) {
            Collections.sort(player.getCards(), new CardComparator());
        }
    }

    private static void fillDeck() {
        undealt = new ArrayList<Card>();
        for (Suit s : Suit.values()) {
            for (int i = 6; i <= 14; i++) {
                undealt.add(new Card(s, i));
            }
        }
    }
}
