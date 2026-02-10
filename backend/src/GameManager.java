package src;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import src.games.IGame;
import src.games.Trumps;
import src.objs.Card;
import src.objs.Player;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GameManager {
    private final int LAST_BONUS = 5;

    private List<Player> players;
    private Scanner s;
    private IGame currentGame;
    private int nextToChoose = 0;
    private int choicesUntilForced = 4;
    private final Lock lock = new ReentrantLock(true);

    public GameManager(List<Player> players) {
        this.players = players;
    }

    public void playGame() throws IOException {
        s = new Scanner(System.in);
        int lastWinner = chooseGame(0);
        
        while (players.get(0).getCards().size() > 0) {
            lastWinner = playTrick(lastWinner);
        }
        players.get(lastWinner).getTeam().addScore(LAST_BONUS);
        players.get(0).getTeam().printScore();
        players.get(1).getTeam().printScore();
        s.close();
    }

    private int chooseGame(int start) throws IOException {
        for (int i = 0; i < players.size(); i++) {
            IGame g = players.get((start + i) % 4).chooseGame(false);
            if (g != null) {
                currentGame = g;
                return i;
            }
        }
        currentGame = players.get(start).chooseGame(true);
        return 0;
    }

    private int playTrick(int player) throws IOException {
        // Store the cards that have been played.
        List<Card> cards = new ArrayList<Card>();

        // Loop through the players.
        for (int i = 0; i < players.size(); i++) {
            if (currentGame.getClass().equals(Trumps.class)) {
                cards.add(players.get(player).getInput(cards, currentGame.getType()));
            } else {
                cards.add(players.get(player).getInput(cards, -1));
            }
            for (int j = 0; j < players.size(); j++) {
                if (j != (player + 1) % 4 || i == 3) {
                    players.get(j).sendPlayedCards(cards);
                }
            }
            player++;
            if (player >= players.size())
                player = 0;
        }

        int winner = (currentGame.wins(cards) + player) % 4;
        System.out.printf("%d Points for %s\n", currentGame.score(cards), players.get(winner).getPlayerName());
        players.get(winner).getTeam().addScore(currentGame.score(cards));
        return winner;

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
        nextToChoose = -1;
    }
}
