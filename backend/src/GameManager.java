package src;

import java.util.List;
import java.util.Scanner;

import src.games.IGame;
import src.objs.Player;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameManager {
    private final int LAST_BONUS = 5;

    private List<Player> players;
    private IGame currentGame;
    private int nextToChoose = 0;
    private int choicesUntilForced = 4;
    private int nextPlayer = -1;

    private final Lock lock = new ReentrantLock(true);

    public GameManager(List<Player> players) {
        this.players = players;
    }

    public int getNextToChoose() {
        int temp = 0;
        lock.lock();
        try {
            temp = nextToChoose;
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
        return temp;
    }

    public boolean isForced() {
        boolean forced = false;
        lock.lock();
        try {
            if (choicesUntilForced <= 0){
                forced = true;
                choicesUntilForced = 4;
                System.out.println("FORCED");
            }
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
        return forced;
    }

    public void incrementChooser() {
        lock.lock();
        try {
            nextToChoose++;
            if (nextToChoose >= 4){
                nextToChoose = 0;
            }
            choicesUntilForced--;
        } catch (Exception e) {
        } finally {
            lock.unlock();
        }
    }

    public IGame getGame() {
        return currentGame;
    }

    public void setGame(IGame g) {
        currentGame = g;
        nextPlayer = nextToChoose;
        nextToChoose = -1;
    }

    public int getNextPlayer(){
        return nextPlayer;
    }
}
