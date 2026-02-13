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
    private static List<Team> teams;
    public static boolean cardsDealt = false;

    public static void main(String[] args) {
        players = new ArrayList<Player>();
        teams = new ArrayList<Team>();
        teams.add(new Team());
        teams.add(new Team());
        fillDeck();
        JassHttpServer.init();
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

    private static void sortCards() {
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

    public static List<Player> getPlayers() {
        return players;
    }

    public static List<Team> getTeams() {
        return teams;
    }

    public static Team getTeam(int idx) {
        for (Team t : teams) {
            if (t.getIndex() == idx) {
                return t;
            }
        }
        Team newTeam = new Team();
        teams.add(newTeam);
        return newTeam;
    }

    public static void addPlayer(Player p) {
        players.add(p);
        p.getTeam().players.add(p);
        if (players.size() == 4) {
            reorderPlayers();
            fillDeck();
            Collections.shuffle(undealt);
            dealCards();
            sortCards();
            cardsDealt = true;
        }
    }

    private static void reorderPlayers() {

        try {
            List<Player> temp = new ArrayList<Player>();
            int t0Pointer = 0;
            int t1Pointer = 0;
            for (int i = 0; i < 2; i++) {
                while (players.get(t0Pointer).getTeam().equals(teams.get(0))) {
                    t0Pointer++;
                }
                temp.add(players.get(t0Pointer));

                while (players.get(t1Pointer).getTeam().equals(teams.get(1))) {
                    t1Pointer++;
                }
                temp.add(players.get(t1Pointer));

                t0Pointer++;
                t1Pointer++;
            }
            players = temp;
            for (int i = 0; i < players.size(); i++) {
                System.out.println(players.get(i).getPlayerName());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
