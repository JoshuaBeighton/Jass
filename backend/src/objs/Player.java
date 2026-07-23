package src.objs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.games.BottomUp;
import src.games.IGame;
import src.games.Middle;
import src.games.TopDown;
import src.games.Trumps;
import src.games.orderings.Jack9Ordering;
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

    public Player(ServerSocket s, List<Team> t, boolean blocking) throws IOException {
        this.cards = new ArrayList<Card>();
        if (!blocking) {
            Thread thread = new Thread(() -> init(s, t));
            thread.start();
        } else {
            init(s, t);
        }

    }

    private void init(ServerSocket s, List<Team> t) {
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
            boolean accept = false;
            while (!accept) {
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
                if (input.charAt(0) == '0' || input.charAt(0) == '1') {
                    this.team = t.get(Integer.parseInt(input.substring(0, 1)));
                    t.get(Integer.parseInt(input.substring(0, 1))).players.add(this);
                    accept = true;
                }
            }
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

    /**
     * Return whether a certain player is allowed to play a given card.
     * @param c The card they want to play.
     * @param played The cards played so far.
     * @param trump The trump suit, or -1 if not a trumps game mode.
     * @return Whether the player is allowed to play a given card.
     */
    public boolean canPlayCard(Card c, List<Card> played, int trump) {
        boolean hasCard = false;
        for (Card card : cards) {
            if (c.equals(card))
                hasCard = true;
        }
        if (!hasCard) {
            return false;
        }

        // Deal with trumps. If the card is a trump of a value above any played trump, it can be played. If the card is not a
        // trump, but the player has a trump,
        // they cannot play it.
        if (trump != -1 && c.getSuit() == Suit.fromIndex(trump)) {
            boolean beats = true;
            Jack9Ordering ordering = new Jack9Ordering(Suit.fromIndex(trump));
            for (Card card : played) {
                if (card.getSuit() == Suit.fromIndex(trump) && ordering.compare(card, c) > 0) {
                    beats = false;
                }
            }
            if (beats) {
                return true;
            }
        }
        // If a card has been played, the first card in the trick doesn't match the suit of the played card, and the played card
        // isn't a trump.
        if (played.size() > 0 && c.getSuit() != played.get(0).getSuit()) {
            // Check if the player has an option to follow suit.
            for (Card card : cards) {
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

                if (canPlayCard(temp, played, code)) {
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
        InputStream reader = socket.getInputStream();
        write("\u001B[2J\u001B[H");
        sendHand();
        writeLine("Choose Game:");
        boolean chosen = false;
        while (!chosen) {
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
                    chosen = true;
                    return new TopDown();
                case 'b':
                    chosen = true;
                    return new BottomUp();
                case 'm':
                    chosen = true;
                    return new Middle();
                case 'r':
                    try {
                        chosen = true;
                        Suit s = Utils.getSuitFromChar(input.charAt(1));
                        return new Trumps(s);
                    }
                    catch (Exception e) {

                    }

                case 'p':
                    if (!forced) {
                        chosen = true;
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
        return null;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("name", name);
        m.put("team", team.getIndex());

        return m;
    }
}
