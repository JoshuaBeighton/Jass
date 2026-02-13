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
            if (choicesUntilForced <= 0){
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
            if (nextToChoose >= 4){
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
        currentGame = g;
        nextPlayer = nextToChoose;
        nextToChoose = -1;
        currentTrick = new ArrayList<Card>();
    }

    public int getNextPlayer(){
        return nextPlayer;
    }
}
