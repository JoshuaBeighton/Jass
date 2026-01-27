package src.objs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import src.games.BottomUp;
import src.games.IGame;
import src.games.Middle;
import src.games.TopDown;
import src.games.Trumps;
import src.utils.Utils;

public class Player {
    private Team team;
    private String name;
    private List<Card> cards;
    private Socket socket;


    public Player(String name, Team t) {
        this.name = name;
        this.team = t;
        this.cards = new ArrayList<Card>();
    }

    public Player(ServerSocket s, Team[] t, boolean blocking) throws IOException {
        this.cards = new ArrayList<Card>();
        if (!blocking) {
            Thread thread = new Thread(() -> init(s, t));
            thread.start();
        } else {
            init(s, t);
        }

    }

    private void init(ServerSocket s, Team[] t) {
        try {
            socket = s.accept();
            writeLine("Name?");
            InputStream reader = socket.getInputStream();
            // The buffer to read to.
            byte[] buffer = new byte[80];

            // Store the amount of bytes acutally read in to the buffer.
            int b = 0;

            // Wait for input.
            while (b == 0) {
                b = reader.read(buffer);
            }
            String input = new String(buffer).trim();
            this.name = input;
            writeLine("TEAM?");


            // The buffer to read to.
            buffer = new byte[2];

            // Store the amount of bytes acutally read in to the buffer.
            b = 0;

            // Wait for input.
            while (b == 0) {
                b = reader.read(buffer);
            }
            input = new String(buffer).trim();
            this.team = t[Integer.parseInt(input)];

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void sendHand() {
        Thread thread = new Thread(() -> {
            try {
                writeLine("Your Hand:");
                for (Card card : cards) {
                    writeLine(card.toString());
                }
                writeLine("\n");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void printHand() {
        System.out.println(name);
        for (Card card : cards) {
            System.out.println(card.toString());
        }
        System.out.println("\n");
    }

    public String getPlayerName() {
        return name;
    }

    public void setPlayerName(String name) {
        this.name = name;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public boolean canPlayCard(Card c, List<Card> played, List<Card> available, int trump) {
        boolean hasCard = false;
        for (Card card : cards) {
            if (c.equals(card))
                hasCard = true;
        }
        if (!hasCard) {
            return false;
        }

        if (played.size() > 0 && c.getSuit() != played.get(0).getSuit() && trump != -1 && Suit.fromIndex(trump) != c.getSuit()) {
            for (Card card : available) {
                if (card.getSuit() == played.get(0).getSuit()) {
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
        if (toRemove != -1) {
            cards.remove(toRemove);
        }
    }

    public Card getInput(List<Card> played, int code) throws IOException {
        InputStream reader = socket.getInputStream();
        Card c = null;
        sendPlayedCards(played);
        sendHand();
        while (c == null) {
            // The buffer to read to.
            byte[] buffer = new byte[8];
            // Store the amount of bytes acutally read in to the buffer.
            int b = 0;

            // Wait for input.
            while (b == 0) {
                b = reader.read(buffer);
            }
            String input = new String(buffer).trim();
            try {
                Card temp = Card.parseCard(input);

                if (canPlayCard(temp, played, cards, code)) {
                    c = temp;
                    removeCard(c);
                } else {
                    sendPlayedCards(played);
                    sendHand();
                    writeLine("You can't play that card!");
                }
            }
            catch (Exception e) {
                sendPlayedCards(played);
                sendHand();
                writeLine("Invalid Card Format!");
            }
        }

        return c;
    }

    public void sendPlayedCards(List<Card> played) throws IOException {
        write("\u001B[2J\u001B[H");
        writeLine("Played So Far:");
        for (Card card : played) {
            writeLine(card.toString());
        }
    }

    private void writeLine(String input) throws IOException {
        write(input + "\n");
    }

    private void write(String input) throws IOException {
        OutputStream writer = socket.getOutputStream();
        writer.write(input.getBytes());
    }

    public IGame chooseGame(boolean forced) throws IOException {
        write("\u001B[2J\u001B[H");
        sendHand();
        writeLine("Choose Game:");
        while (true) {
            InputStream reader = socket.getInputStream();
            // The buffer to read to.
            byte[] buffer = new byte[8];
            // Store the amount of bytes acutally read in to the buffer.
            int b = 0;

            // Wait for input.
            while (b == 0) {
                b = reader.read(buffer);
            }
            String input = new String(buffer).trim();

            switch (input.charAt(0)) {
                case 't':
                    return new TopDown();
                case 'b':
                    return new BottomUp();
                case 'm':
                    return new Middle();
                case 'r':
                    try {
                        Suit s = Utils.getSuitFromChar(input.charAt(1));
                        return new Trumps(s);
                    }
                    catch (Exception e) {

                    }

                case 'p':
                    if (!forced) {
                        return null;
                    }
                    break;
                default:
                    break;
            }

            write("\u001B[2J\u001B[H");
            writeLine("****Invalid Choice****");
            sendHand();
            writeLine("Choose Game:");
        }

    }
}
