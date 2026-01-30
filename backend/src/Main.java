package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.io.*;
import java.net.*;

import src.objs.Card;
import src.objs.Player;
import src.objs.Suit;
import src.objs.Team;
import src.utils.CardComparator;

public class Main {
    private static List<Card> undealt;
    private static List<Player> players;
    private static List<Team> teams;

    public static void main(String[] args) {
        players = new ArrayList<Player>();
        teams = new ArrayList<Team>();
        teams.add(new Team());
        teams.add(new Team());
        netInit();
        fillDeck();
        // Shuffle Deck
        Collections.shuffle(undealt);
        dealCards();
        sortCards();
        sendCards();

        GameManager m = new GameManager(players);
        try {
            m.playGame();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void netInit() {
        int portNo = 8000;
        try {
            ServerSocket server = new ServerSocket(5000);
            for (int i = 0; i < 4; i++) {
                System.out.println("Waiting on port 5000!");

                Socket socket = server.accept();
                InputStream reader = socket.getInputStream();
                boolean success = false;
                while (!success) {
                    // The buffer to read to.
                    byte[] buffer = new byte[5];

                    // Store the amount of bytes acutally read in to the buffer.
                    int b = 0;

                    // Wait for input.
                    while (b == 0) {
                        b = reader.read(buffer);
                    }
                    String input = new String(buffer).trim();
                    if (input.equals("PORT?")) {
                        String toWrite = String.valueOf(portNo) + "\n";
                        OutputStream out = socket.getOutputStream();
                        out.write(toWrite.getBytes());
                        addPlayer(portNo, i == 3);
                        portNo++;
                        success = true;
                    } else {
                        String toWrite = "Expected \"PORT?\"\n";
                        OutputStream out = socket.getOutputStream();
                        out.write(toWrite.getBytes());
                    }
                }
                socket.close();
            }
            server.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addPlayer(int portNo, boolean blocking) throws IOException {
        ServerSocket server = new ServerSocket(portNo);
        Player p = new Player(server, teams, blocking);
        players.add(p);
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

    private static void sendCards() {
        for (Player player : players) {
            player.sendHand();
        }
    }
}
