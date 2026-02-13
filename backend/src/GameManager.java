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
            temp = currentTrick;
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
            currentTrick = new ArrayList<Card>();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getNextPlayer() {
        return nextPlayer;
    }

    public void playCard(String s, List<Player> players) {
        int idx = -1;
        try {
            Card candidate = Card.parseCard(s);
            for (int i = 0; i < players.get(nextPlayer).getCards().size(); i++) {
                if (players.get(nextPlayer).getCards().get(i).equals(candidate)) {
                    idx = i;
                    trickLock.lock();
                    nextPlayer++;
                    if (nextPlayer > 3) {
                        nextPlayer = 0;
                    }
                    currentTrick.add(players.get(nextPlayer).getCards().get(i));
                    trickLock.unlock();
                }
            }
            if (idx > 0) {
                players.get(nextPlayer).getCards().remove(idx);
                System.out.println("Cards Played: " + currentTrick.size());


            } else {
                System.out.println("Couldn't find card");
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
