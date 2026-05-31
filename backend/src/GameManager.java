package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import src.games.IGame;
import src.objs.Card;
import src.objs.Player;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameManager {
    private final int LAST_BONUS = 5;

    private IGame currentGame;
    private int nextToChoose = 0;
    private int choicesUntilForced = 4;
    private int nextPlayer = -1;
    private List<Card> currentTrick;

    private final Lock nextToChooseLock = new ReentrantLock(true);
    private final Lock trickLock = new ReentrantLock(true);

    public GameManager() {
    }

    public int getNextToChoose() {
        int temp = 0;
        nextToChooseLock.lock();
        try {
            temp = nextToChoose;
        } catch (Exception e) {
        } finally {
            nextToChooseLock.unlock();
        }
        return temp;
    }

    public List<Card> getCurrentTrick() {
        List<Card> temp = null;
        trickLock.lock();
        try {
            temp = currentTrick;
        } catch (Exception e) {
        } finally {
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
        } catch (Exception e) {
        } finally {
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
        } catch (Exception e) {
        } finally {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getNextPlayer() {
        return nextPlayer;
    }

    public boolean playCard(String s, List<Player> players) {
        System.out.println("Before Playing");
        players.get(nextPlayer).printHand();
        int idx = -1;
        try {
            Card candidate = Card.parseCard(s);
            for (int i = 0; i < players.get(nextPlayer).getCards().size(); i++) {
                if (players.get(nextPlayer).getCards().get(i).equals(candidate)) {
                    // Found the correct card in their hand.
                    idx = i;

                    if (players.get(nextPlayer).canPlayCard(candidate, currentTrick, players.get(nextPlayer).getCards(),
                            -1) == false) {
                        System.out.println("Cannot play that card.");
                        return false;
                    }
                    trickLock.lock();
                    currentTrick.add(candidate);
                    trickLock.unlock();
                }
            }
            if (idx > 0) {
                players.get(nextPlayer).getCards().remove(idx);
                System.out.println("Cards Played: " + currentTrick.size());

            } else {
                System.out.printf("Couldn't find card %s in player %s\nHand:", s,
                        Main.getPlayers().get(nextPlayer).getPlayerName());
                Main.getPlayers().get(nextPlayer).printHand();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("After Playing");
        players.get(nextPlayer).printHand();
        nextPlayer = (nextPlayer + 1) % 4;
        return true;
    }

    public void resetTrick() {
        if (currentTrick.size() >= 4) {
            System.out.println("Clearing");
            // Get the index of the card that won the trick, add it to the start player
            // (which will be the next player as it's wrapped around)
            int winner = (currentGame.wins(currentTrick) + nextPlayer) % 4;
            Main.getPlayers().get(winner).getTeam().addScore(currentGame.score(currentTrick));
            currentTrick.clear();
            nextPlayer = winner;
        }
        else{
            System.out.println("Already cleared");
        }
    }
}
