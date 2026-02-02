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
            System.out.println("Dealt Cards");
        }
    }

    private static void reorderPlayers() {
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
    }

    /*
     * private static void netInit() { int portNo = 8000; try { ServerSocket server = new ServerSocket(5000); for (int i =
     * 0; i < 4; i++) { System.out.println("Waiting on port 5000!");
     * 
     * Socket socket = server.accept(); InputStream reader = socket.getInputStream(); boolean success = false; while
     * (!success) { // The buffer to read to. byte[] buffer = new byte[5];
     * 
     * // Store the amount of bytes acutally read in to the buffer. int b = 0;
     * 
     * // Wait for input. while (b == 0) { b = reader.read(buffer); } String input = new String(buffer).trim(); if
     * (input.equals("PORT?")) { String toWrite = String.valueOf(portNo) + "\n"; OutputStream out =
     * socket.getOutputStream(); out.write(toWrite.getBytes()); addPlayer(portNo, i == 3); portNo++; success = true; } else
     * { String toWrite = "Expected \"PORT?\"\n"; OutputStream out = socket.getOutputStream();
     * out.write(toWrite.getBytes()); } } socket.close(); } server.close();
     * 
     * } catch (IOException e) { e.printStackTrace(); } }
     * 
     * private static void addPlayer(int portNo, boolean blocking) throws IOException { ServerSocket server = new
     * ServerSocket(portNo); Player p = new Player(server, teams, blocking); players.add(p); }
     * 
     * private static void sendCards() { for (Player player : players) { player.sendHand(); } }
     */
}
