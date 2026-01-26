package src;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.naming.InvalidNameException;

import src.games.IGame;
import src.games.Middle;
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

    public void playGame() {
        s = new Scanner(System.in);
        currentGame = new Trumps();
        currentGame.setup(Suit.CLUBS.index());
        int lastWinner = 0;
        while (players.get(0).getCards().size() > 0) {
            lastWinner = playTrick(lastWinner);
        }
        players.get(lastWinner).getTeam().addScore(LAST_BONUS);
        players.get(0).getTeam().printScore();
        players.get(1).getTeam().printScore();
        s.close();
    }

    public int playTrick(int player) {
        // Store the cards that have been played.
        List<Card> cards = new ArrayList<Card>();

        // Loop through the players.
        for (int i = 0; i < players.size(); i++) {
            // Loop until successfully parsed the input.
            boolean success = false;
            while (!success) {
                // If cards have been played yet, output them.
                if (cards.size() > 0) {
                    System.out.println("Played so far:");
                    for (Card card : cards) {
                        System.out.println(card.toString());
                    }
                }

                System.out.println("\nYour Hand:");
                // Print the current player's hand.
                players.get(player).printHand();

                try {
                    // Get which card was played
                    Card played = Card.parseCard(s.nextLine());
                    if (players.get(player).canPlayCard(played, cards, players.get(player).getCards(), Suit.CLUBS.index())) {
                        cards.add(played);
                        players.get(player).removeCard(played);
                        success = true;
                    }
                } catch (InvalidNameException e) {
                    System.err.println("Invalid Card Format");
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            System.out.print("\u001B[2J\u001B[H");
            System.out.flush();

            player++;
            if (player >= players.size())
                player = 0;
        }

        int winner = (currentGame.wins(cards) + player) % 4;
        System.out.printf("%d Points for %s\n", currentGame.score(cards), players.get(winner).getName());
        players.get(winner).getTeam().addScore(currentGame.score(cards));
        return winner;
    }
}
