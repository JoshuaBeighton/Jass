package src;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import src.games.IGame;
import src.games.TopDown;
import src.games.Trumps;
import src.objs.Card;
import src.objs.Player;
import src.objs.Suit;

public class GameManager {
    private final int LAST_BONUS = 5;

    private List<Player> players;
    private Scanner s;

    public GameManager(List<Player> players) {
        this.players = players;
    }

    IGame currentGame;

    public void playGame() throws IOException {
        s = new Scanner(System.in);
        currentGame = new TopDown();
        // currentGame = chooseGame(0);
        int lastWinner = 0;
        while (players.get(0).getCards().size() > 0) {
            lastWinner = playTrick(lastWinner);
        }
        players.get(lastWinner).getTeam().addScore(LAST_BONUS);
        players.get(0).getTeam().printScore();
        players.get(1).getTeam().printScore();
        s.close();
    }

    private IGame chooseGame(int start) throws IOException {
        for (int i = 0; i < players.size(); i++) {
            IGame g = players.get((start + i) % 4).chooseGame(false);
            if (g != null)
                return g;
        }
        return players.get(start).chooseGame(true);
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
}
