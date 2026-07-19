package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import src.games.IGame;
import src.objs.Card;
import src.objs.Player;
import src.utils.CardComparator;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import src.objs.Suit;
import src.objs.Team;

public class GameManager {
    public static final String[] GAMES = {
            "Top Down", "Bottom Up", "Middle", "Trumps"
    };


    private final int LAST_BONUS = 5;

    private IGame currentGame;
    private int nextToChoose = 0;
    private int choicesUntilForced = 4;
    private int nextPlayer = -1;
    private List<Card> currentTrick;

    private final Lock nextToChooseLock = new ReentrantLock(true);
    private final Lock trickLock = new ReentrantLock(true);
    private int trickCount = 0;

    private List<Card> undealt;
    private List<Player> players;
    private List<Team> teams;
    public boolean cardsDealt = false;

    public GameManager() {
        players = new ArrayList<Player>();
        teams = new ArrayList<Team>();
        teams.add(new Team());
        teams.add(new Team());
        fillDeck();
    }

    private void fillDeck() {
        undealt = new ArrayList<Card>();
        for (Suit s : Suit.values()) {
            for (int i = 6; i <= 14; i++) {
                undealt.add(new Card(s, i));
            }
        }
    }

    private void dealCards() {
        // Ensure each player starts with an empty hand.
        for (Player p : players) {
            p.getCards().clear();
        }

        int playerIndex = 0;

        while (!undealt.isEmpty()) {
            // Mimic traditional Jass dealing, where 3 cards are dealt to a player at once.
            players.get(playerIndex).getCards().addAll(undealt.subList(0, 3));
            undealt.removeAll(undealt.subList(0, 3));
            playerIndex++;
            if (playerIndex >= players.size())
                playerIndex = 0;
        }
    }

    private void sortCards() {
        for (Player player : players) {
            Collections.sort(player.getCards(), new CardComparator());
        }
    }



    public List<Player> getPlayers() {
        return players;
    }

    public List<Team> getTeams() {
        return teams;
    }


    public void addPlayer(Player p) {
        players.add(p);
        p.getTeam().players.add(p);
        if (players.size() == 4) {
            reorderPlayers();
            fillDeck();
            Collections.shuffle(undealt);
            dealCards();
            sortCards();
            cardsDealt = true;
            for (Player pl : players) {
                System.out.println(pl.getPlayerName());
                pl.printHand();
            }
        }
    }

    private void reorderPlayers() {

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

    public int getNextToChoose() {
        int temp = 0;
        nextToChooseLock.lock();
        try {
            temp = nextToChoose;
        }
        catch (Exception e) {
        }
        finally {
            nextToChooseLock.unlock();
        }
        return temp;
    }

    public List<Card> getCurrentTrick() {
        List<Card> temp = null;
        trickLock.lock();
        try {
            temp = new ArrayList<Card>(currentTrick);
        }
        catch (Exception e) {
        }
        finally {
            trickLock.unlock();
        }
        return temp;
    }

    public boolean isForced() {
        boolean forced = false;
        nextToChooseLock.lock();
        try {
            if (choicesUntilForced <= 0) {
                forced = true;
                choicesUntilForced = 4;
                System.out.println("FORCED");
            }
        }
        catch (Exception e) {
        }
        finally {
            nextToChooseLock.unlock();
        }
        return forced;
    }

    public void incrementChooser() {
        nextToChooseLock.lock();
        try {
            nextToChoose++;
            if (nextToChoose >= 4) {
                nextToChoose = 0;
            }
            choicesUntilForced--;
        }
        catch (Exception e) {
        }
        finally {
            nextToChooseLock.unlock();
        }
    }

    public IGame getGame() {
        return currentGame;
    }

    public void setGame(IGame g) {
        try {
            currentGame = g;
            nextPlayer = nextToChoose;
            nextToChoose = (nextToChoose - 1) % 4;
            currentTrick = new ArrayList<Card>();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getNextPlayer() {
        return nextPlayer;
    }

    public boolean playCard(String s) {
        try {
            System.out.println("Cards played: " + currentTrick.size());
            Card candidate = Card.parseCard(s);
            Player currentPlayer = players.get(nextPlayer);

            if (!currentPlayer.canPlayCard(candidate, currentTrick, currentGame.getSuit())) {
                System.out.println("Cannot play that card.");
                return false;
            }
            trickLock.lock();
            currentTrick.add(candidate);
            trickLock.unlock();

            currentPlayer.removeCard(candidate);

        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        nextPlayer = (nextPlayer + 1) % 4;
        return true;
    }

    public void resetTrick() {
        if (currentTrick.size() >= 4) {
            // Get the index of the card that won the trick, add it to the start player
            // (which will be the next player as it's wrapped around)
            int winner = (currentGame.wins(currentTrick) + nextPlayer) % 4;
            players.get(winner).getTeam().addScore(currentGame.score(currentTrick));
            currentTrick.clear();
            nextPlayer = winner;
            trickCount++;
            System.out.println("Trick Count: " + trickCount);
            if (trickCount >= 9) {
                players.get(winner).getTeam().addScore(LAST_BONUS);
                System.out.println("Game Over! Team 0: " + teams.get(0).getScore() + " Team 1: " + teams.get(1).getScore());
                resetGame();
            }
        } else {
            System.out.println("Already cleared");
        }
    }

    public void resetGame() {
        players.get(nextToChoose).getTeam().setScore(currentGame.getName(), players.get(nextToChoose).getTeam().getScore());
        System.out.println("Resetting Game");
        fillDeck();
        Collections.shuffle(undealt);
        dealCards();
        sortCards();
        trickCount = 0;
        currentGame = null;
        nextToChoose = 0;
        choicesUntilForced = 4;
        nextPlayer = -1;
        currentTrick.clear();
        teams.get(0).resetScore();
        teams.get(1).resetScore();
    }
}
