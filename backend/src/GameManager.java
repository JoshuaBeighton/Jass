package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import src.games.IGame;
import src.games.Misere;
import src.objs.Card;
import src.objs.Player;
import src.objs.Suit;
import src.objs.Team;
import src.utils.CardComparator;

/**
 * Manages game state for a single Jass room.
 */
public class GameManager {
    public static final String[] GAMES = {
            "Top Down", "Bottom Up", "Misere", "Middle", "Trumps", "Slalom", "FiveFour", "Elephant", "Saint Legier", "Jack9", "Rio"
    };

    private List<Integer> activeMultipliers;
    private Map<Integer, List<String>> assignments;

    private final int LAST_BONUS = 5;
    public boolean gamesConfigrured = false;
    private IGame currentGame;
    private int nextToChoose = 0;
    private int choicesUntilForced = 4;
    private int nextPlayer = -1;
    private int gameCaller = -1;
    private int trickWinner = -1;

    private List<Card> currentTrick;

    private final Lock nextToChooseLock = new ReentrantLock(true);
    private final Lock trickLock = new ReentrantLock(true);
    private final Lock assignmentLock = new ReentrantLock(true);
    private int trickCount = 0;

    private List<Card> undealt;
    private List<Player> players;
    private List<Team> teams;
    public boolean cardsDealt = false;

    public boolean visible;

    /**
     * Creates a new GameManager for a room.
     *
     * @param visible whether the room should be publicly discoverable
     */
    public GameManager(boolean visible) {
        players = new ArrayList<Player>();
        teams = new ArrayList<Team>();
        teams.add(new Team(0));
        teams.add(new Team(1));

        activeMultipliers = new ArrayList<>();
        assignments = new HashMap<>();

        // Initialize default multipliers 1..5
        for (int i = 1; i <= 5; i++) {
            activeMultipliers.add(i);
            assignments.put(i, new ArrayList<>());
        }

        fillDeck();
        this.visible = visible;
    }

    public List<Integer> getActiveMultipliers() {
        assignmentLock.lock();
        try {
            return new ArrayList<>(activeMultipliers);
        }
        finally {
            assignmentLock.unlock();
        }
    }

    public Map<Integer, List<String>> getAssignments() {
        assignmentLock.lock();
        try {
            Map<Integer, List<String>> copy = new HashMap<>();
            assignments.forEach((k, v) -> copy.put(k, new ArrayList<>(v)));
            return copy;
        }
        finally {
            assignmentLock.unlock();
        }
    }

    /**
     * Dynamically updates multipliers and game assignments from client input.
     */
    public void updateAssignments(List<Integer> newMultipliers, Map<Integer, List<String>> newAssignments) {
        assignmentLock.lock();
        try {
            this.activeMultipliers = new ArrayList<>(newMultipliers);
            this.assignments = new HashMap<>();
            newAssignments.forEach((k, v) -> this.assignments.put(k, new ArrayList<>(v)));
        }
        finally {
            assignmentLock.unlock();
        }
    }

    /**
     * Returns the multiplier value assigned to a specific game mode name.
     */
    public int getMultiplierForGame(String gameName) {
        assignmentLock.lock();
        try {
            for (Map.Entry<Integer, List<String>> entry : assignments.entrySet()) {
                if (entry.getValue().contains(gameName)) {
                    return entry.getKey();
                }
            }
            return 1;
        }
        finally {
            assignmentLock.unlock();
        }
    }

    public int getTrickWinner() {
        return trickWinner;
    }

    /**
     * Populates the undealt deck.
     */
    private void fillDeck() {
        undealt = new ArrayList<Card>();
        for (Suit s : Suit.values()) {
            for (int i = 6; i <= 14; i++) {
                undealt.add(new Card(s, i));
            }
        }
    }

    /**
     * Deals cards to players in groups of three until the deck is empty.
     */
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

    /**
     * Sorts each player's hand using the game comparator.
     */
    private void sortCards() {
        for (Player player : players) {
            Collections.sort(player.getCards(), new CardComparator());
        }
    }

    /**
     * Returns the list of players in the room.
     *
     * @return current player list
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Returns the list of teams in the room.
     *
     * @return current team list
     */
    public List<Team> getTeams() {
        return teams;
    }

    /**
     * Adds a player to the room and triggers dealing when four players have joined.
     *
     * @param p player to add
     */
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

    /**
     * Reorders players so partners alternate in the seating order.
     */
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

    /**
     * Returns the index of the next player who may choose a game.
     *
     * @return next chooser index
     */
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

    /**
     * Waits for the chooser to change from the supplied index, but returns promptly if no change occurs before the timeout
     * elapses.
     *
     * @param lastIndex the chooser index seen by the client
     * @param timeoutMillis the maximum time to wait in milliseconds
     * @return the current chooser index after waiting
     */
    public int waitForChooserChange(int lastIndex, long timeoutMillis) {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            int current = getNextToChoose();
            if (current != lastIndex) {
                return current;
            }
            try {
                Thread.sleep(50);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return getNextToChoose();
            }
        }
        return getNextToChoose();
    }

    /**
     * Returns a copy of the current trick cards.
     *
     * @return current trick list
     */
    public List<Card> getCurrentTrick() {
        List<Card> temp = new ArrayList<Card>();
        trickLock.lock();
        try {
            if (currentTrick != null) {
                temp = new ArrayList<Card>(currentTrick);
            }
        }
        catch (Exception e) {
        }
        finally {
            trickLock.unlock();
        }
        return temp;
    }

    /**
     * Waits for the current trick size to change from the supplied value, but returns promptly if no change occurs before
     * the timeout elapses.
     *
     * @param lastSize the trick size seen by the client
     * @param timeoutMillis the maximum time to wait in milliseconds
     * @return the current trick size after waiting
     */
    public int waitForTrickChange(int lastSize, long timeoutMillis) {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < deadline) {
            int currentSize = getCurrentTrick().size();
            if (currentSize != lastSize) {
                return currentSize;
            }
            try {
                Thread.sleep(25);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return getCurrentTrick().size();
            }
        }
        return getCurrentTrick().size();
    }

    /**
     * Checks whether the current chooser is forced to select a game.
     *
     * @return true if the chooser must choose a game, false otherwise
     */
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

    /**
     * Advances the chooser index and decrements the forced-choice counter.
     */
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

    /**
     * Returns the currently selected game.
     *
     * @return the current game or null if none is selected
     */
    public IGame getGame() {
        return currentGame;
    }

    /**
     * Sets the current game and initializes trick state.
     *
     * @param g the chosen game mode
     */
    public void setGame(IGame g) {
        try {
            currentGame = g;
            nextPlayer = nextToChoose;
            gameCaller = nextToChoose;
            nextToChoose = (nextToChoose - 1) % 4;
            currentTrick = new ArrayList<Card>();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the index of the player who should play next.
     *
     * @return next player index
     */
    public int getNextPlayer() {
        return nextPlayer;
    }

    /**
     * Attempts to play the specified card string for the current player.
     *
     * @param s the card notation to play
     * @return true if the card was successfully played, false otherwise
     */
    public boolean playCard(String s) {
        try {
            if (currentGame == null || currentTrick == null) {
                System.out.println("No active trick to play into.");
                return false;
            }

            trickLock.lock();
            try {
                System.out.println("Cards played: " + currentTrick.size());
                Card candidate = Card.parseCard(s);
                Player currentPlayer = players.get(nextPlayer);

                if (!currentPlayer.canPlayCard(candidate, currentTrick, currentGame)) {
                    System.out.println("Cannot play that card.");
                    return false;
                }
                currentTrick.add(candidate);
                currentPlayer.removeCard(candidate);
            }
            finally {
                trickLock.unlock();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        nextPlayer = (nextPlayer + 1) % 4;
        return true;
    }

    /**
     * Resolves the current trick, awards points to the winning team, and advances the next player.
     */
    public void resetTrick() {
        trickLock.lock();
        try {
            if (currentTrick != null && currentTrick.size() >= 4) {
                // Get the index of the card that won the trick, add it to the start player
                // (which will be the next player as it's wrapped around)
                int winner = (currentGame.wins(currentTrick, trickCount) + nextPlayer) % 4;
                if (currentGame instanceof Misere) {
                    players.get((winner + 1) % 4).getTeam().addScore(currentGame.score(currentTrick));
                } else {
                    players.get(winner).getTeam().addScore(currentGame.score(currentTrick));
                }
                currentTrick.clear();
                nextPlayer = winner;
                trickWinner = winner;
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
        finally {
            trickLock.unlock();
        }
    }

    /**
     * Resets the game state and prepares a new round.
     */
    public void resetGame() {
        for (Entry<Integer, List<String>> t : assignments.entrySet()) {
            if (t.getValue().contains(currentGame.getName())) {
                players.get(gameCaller).getTeam().setScore(t.getKey(), players.get(gameCaller).getTeam().getScore());
            }
        }
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


    public void resetMatch(String toRemove) {
        players.clear();
        teams.get(0).resetMatch(activeMultipliers);
        teams.get(1).resetMatch(activeMultipliers);
        gamesConfigrured = false;
    }

    public void confirmConfig() {
        gamesConfigrured = true;
        teams.get(0).configureGames(activeMultipliers);
        teams.get(1).configureGames(activeMultipliers);
    }
}
