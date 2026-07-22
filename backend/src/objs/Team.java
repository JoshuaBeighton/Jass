package src.objs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.GameManager;

public class Team {
    public List<Player> players;
    private int currentScore;
    private int index;
    private Map<String, Integer> gameScores;

    public List<String> getGamesAvailable() {
        List<String> available = new ArrayList<String>();
        gameScores.forEach((game, score) -> {
            if (score == -1) {
                available.add(game);
            }
        });
        return available;
    }

    public Team(int index) {
        players = new ArrayList<Player>();
        currentScore = 0;
        this.index = index;
        gameScores = new HashMap<String, Integer>();
        for (String game : GameManager.GAMES) {
            gameScores.put(game, -1);
        }
    }

    public void addScore(int val) {
        currentScore += val;
    }

    public void resetScore() {
        currentScore = 0;
    }

    public int getScore() {
        return currentScore;
    }

    public int getIndex() {
        return index;
    }

    public void printScore() {
        System.out.printf("Team %s & %s:\t%d\n", players.get(0).getPlayerName(), players.get(1).getPlayerName(), currentScore);
    }

    public void setScore(String mode, int score) {
        gameScores.put(mode, score);
    }

    public int getScore(String mode) {
        return gameScores.get(mode);
    }
}
